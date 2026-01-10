package com.tmforum.openapi.mapper;

import com.tmforum.openapi.dto.*;
import com.tmforum.openapi.model.Customer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Component
public class CustomerMapper {
    
    public Customer toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }
        
        Customer customer = new Customer();
        
        // Set firstName and lastName directly from request
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setDocumentType(request.getDocumentType());
        customer.setDocumentNumber(request.getDocumentNumber());
        
        // Extract email from contactMedium
        if (request.getContactMedium() != null) {
            Optional<ContactMedium> emailContact = request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getEmailAddress()))
                    .findFirst();
            if (emailContact.isPresent()) {
                customer.setEmail(emailContact.get().getEmailAddress());
            }
            
            // Extract phone numbers
            Optional<ContactMedium> phoneContact = request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getPhoneNumber()) || StringUtils.hasText(cm.getMobileNumber()))
                    .findFirst();
            if (phoneContact.isPresent()) {
                customer.setPhoneNumber(phoneContact.get().getPhoneNumber());
                customer.setMobileNumber(phoneContact.get().getMobileNumber());
            }
            
            // Extract address
            Optional<ContactMedium> addressContact = request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getStreet1()))
                    .findFirst();
            if (addressContact.isPresent()) {
                ContactMedium addr = addressContact.get();
                StringBuilder addressBuilder = new StringBuilder();
                if (StringUtils.hasText(addr.getStreet1())) {
                    addressBuilder.append(addr.getStreet1());
                }
                if (StringUtils.hasText(addr.getStreet2())) {
                    if (addressBuilder.length() > 0) addressBuilder.append(", ");
                    addressBuilder.append(addr.getStreet2());
                }
                if (StringUtils.hasText(addr.getCity())) {
                    if (addressBuilder.length() > 0) addressBuilder.append(", ");
                    addressBuilder.append(addr.getCity());
                }
                if (StringUtils.hasText(addr.getPostCode())) {
                    if (addressBuilder.length() > 0) addressBuilder.append(" ");
                    addressBuilder.append(addr.getPostCode());
                }
                customer.setAddress(addressBuilder.toString());
            }
        }
        
        // Extract document info from engagedParty if available
        if (request.getEngagedParty() != null && StringUtils.hasText(request.getEngagedParty().getId())) {
            // Use engagedParty ID as document number if no other source
            if (!StringUtils.hasText(customer.getDocumentNumber())) {
                customer.setDocumentNumber(request.getEngagedParty().getId());
            }
        }
        
        return customer;
    }
    
    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setHref("/api/customer/" + customer.getId());
        response.setSchemaLocation("/tmf-api/customer/v5/schema/Customer");
        
        response.setDocumentType(customer.getDocumentType());
        response.setDocumentNumber(customer.getDocumentNumber());

        // Concatenate firstName and lastName to form name
        StringBuilder nameBuilder = new StringBuilder();
        if (StringUtils.hasText(customer.getFirstName())) {
            nameBuilder.append(customer.getFirstName());
        }
        if (StringUtils.hasText(customer.getLastName())) {
            if (nameBuilder.length() > 0) {
                nameBuilder.append(" ");
            }
            nameBuilder.append(customer.getLastName());
        }
        response.setName(nameBuilder.toString().trim());
        
        // Create engagedParty
        PartyRef engagedParty = new PartyRef();
        engagedParty.setId(customer.getId());
        engagedParty.setHref("/api/party/" + customer.getId());
        engagedParty.setName(response.getName());
        engagedParty.setReferredType("Individual");
        response.setEngagedParty(engagedParty);
        
        // Create contactMedium from customer data
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setType("EmailContactMedium");
        contactMedium.setEmailAddress(customer.getEmail());
        contactMedium.setPhoneNumber(customer.getPhoneNumber());
        contactMedium.setMobileNumber(customer.getMobileNumber());
        
        // Parse address if available
        if (StringUtils.hasText(customer.getAddress())) {
            // Simple parsing - in production, use a proper address parser
            contactMedium.setStreet1(customer.getAddress());
        }
        
        response.setContactMedium(List.of(contactMedium));
        response.setStatus("Active");
        response.setCreationDate(customer.getCreationDate());
        response.setUpdateDate(customer.getUpdateDate());
        
        return response;
    }
    
    public void updateEntityFromRequest(CustomerRequest request, Customer customer) {
        if (request == null || customer == null) {
            return;
        }
        
        // Update firstName and lastName directly from request
        if (StringUtils.hasText(request.getFirstName())) {
            customer.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            customer.setLastName(request.getLastName());
        }

        if (request.getDocumentType() != null) {
            customer.setDocumentType(request.getDocumentType());
        }
        if (StringUtils.hasText(request.getDocumentNumber())) {
            customer.setDocumentNumber(request.getDocumentNumber());
        }
        
        // Update contact info
        if (request.getContactMedium() != null) {
            request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getEmailAddress()))
                    .findFirst()
                    .ifPresent(cm -> customer.setEmail(cm.getEmailAddress()));
            
            request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getPhoneNumber()) || StringUtils.hasText(cm.getMobileNumber()))
                    .findFirst()
                    .ifPresent(cm -> {
                        customer.setPhoneNumber(cm.getPhoneNumber());
                        customer.setMobileNumber(cm.getMobileNumber());
                    });
        }
        
        // Update status
        if (StringUtils.hasText(request.getStatus())) {
            // Status is stored but not mapped to Customer entity fields directly
            // Could be extended if needed
        }
    }
    
    public void updateEntityFromPatchRequest(CustomerPatchRequest request, Customer customer) {
        if (request == null || customer == null) {
            return;
        }
        
        // Only update fields that are provided (PATCH semantics)
        if (StringUtils.hasText(request.getFirstName())) {
            customer.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            customer.setLastName(request.getLastName());
        }
        
        if (request.getContactMedium() != null && !request.getContactMedium().isEmpty()) {
            request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getEmailAddress()))
                    .findFirst()
                    .ifPresent(cm -> customer.setEmail(cm.getEmailAddress()));
            
            request.getContactMedium().stream()
                    .filter(cm -> StringUtils.hasText(cm.getPhoneNumber()) || StringUtils.hasText(cm.getMobileNumber()))
                    .findFirst()
                    .ifPresent(cm -> {
                        if (StringUtils.hasText(cm.getPhoneNumber())) {
                            customer.setPhoneNumber(cm.getPhoneNumber());
                        }
                        if (StringUtils.hasText(cm.getMobileNumber())) {
                            customer.setMobileNumber(cm.getMobileNumber());
                        }
                    });
        }
    }
    
    // Methods for CustomerRequestV1 (legacy v1 API)
    
    public Customer toEntityV1(CustomerRequestV1 request) {
        if (request == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setDocumentType(request.getDocumentType());
        customer.setDocumentNumber(request.getDocumentNumber());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setMobileNumber(request.getMobileNumber());
        customer.setAddress(request.getAddress());
        
        return customer;
    }
    
    public void updateEntityFromRequestV1(CustomerRequestV1 request, Customer customer) {
        if (request == null || customer == null) {
            return;
        }
        
        if (StringUtils.hasText(request.getFirstName())) {
            customer.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            customer.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            customer.setEmail(request.getEmail());
        }
        if (request.getDocumentType() != null) {
            customer.setDocumentType(request.getDocumentType());
        }
        if (StringUtils.hasText(request.getDocumentNumber())) {
            customer.setDocumentNumber(request.getDocumentNumber());
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getMobileNumber())) {
            customer.setMobileNumber(request.getMobileNumber());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }
    }
    
    public CustomerResponseV1 toResponseV1(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerResponseV1 response = new CustomerResponseV1();
        response.setId(customer.getId());
        response.setDocumentType(customer.getDocumentType());
        response.setDocumentNumber(customer.getDocumentNumber());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setEmail(customer.getEmail());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setMobileNumber(customer.getMobileNumber());
        response.setAddress(customer.getAddress());
        response.setCreationDate(customer.getCreationDate());
        response.setUpdateDate(customer.getUpdateDate());
        
        return response;
    }
}
