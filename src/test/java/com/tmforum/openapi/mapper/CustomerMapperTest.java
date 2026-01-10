package com.tmforum.openapi.mapper;

import com.tmforum.openapi.dto.*;
import com.tmforum.openapi.model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {
    
    private CustomerMapper mapper;
    private CustomerRequest request;
    private CustomerPatchRequest patchRequest;
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        mapper = new CustomerMapper();
        
        // Create PartyRef
        PartyRef engagedParty = new PartyRef();
        engagedParty.setId("party-1");
        engagedParty.setName("Juan Perez");
        engagedParty.setReferredType("Individual");
        
        // Create ContactMedium
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setType("EmailContactMedium");
        contactMedium.setEmailAddress("juan@example.com");
        contactMedium.setPhoneNumber("1234567890");
        contactMedium.setMobileNumber("0987654321");
        contactMedium.setStreet1("Street 123");
        contactMedium.setCity("City");
        contactMedium.setPostCode("12345");
        
        // Create CustomerRequest
        request = new CustomerRequest();
        request.setFirstName("Juan");
        request.setLastName("Perez");
        request.setEngagedParty(engagedParty);
        request.setContactMedium(Arrays.asList(contactMedium));
        request.setStatus("Active");
        
        // Create CustomerPatchRequest
        patchRequest = new CustomerPatchRequest();
        patchRequest.setFirstName("Juan Carlos");
        patchRequest.setLastName("Perez");
        
        ContactMedium updatedContact = new ContactMedium();
        updatedContact.setEmailAddress("juan.carlos@example.com");
        patchRequest.setContactMedium(Arrays.asList(updatedContact));
        
        // Create Customer entity
        customer = new Customer();
        customer.setId("1");
        customer.setFirstName("Juan");
        customer.setLastName("Perez");
        customer.setEmail("juan@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setMobileNumber("0987654321");
        customer.setAddress("Street 123");
        customer.setCreationDate(LocalDateTime.now());
        customer.setUpdateDate(LocalDateTime.now());
    }
    
    @Test
    void testToEntity_Success() {
        Customer result = mapper.toEntity(request);
        
        assertNotNull(result);
        assertEquals("Juan", result.getFirstName());
        assertEquals("Perez", result.getLastName());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals("0987654321", result.getMobileNumber());
        assertTrue(result.getAddress().contains("Street 123"));
    }
    
    @Test
    void testToEntity_WithFirstNameAndLastName() {
        request.setFirstName("Maria");
        request.setLastName("Garcia");
        
        Customer result = mapper.toEntity(request);
        
        assertNotNull(result);
        assertEquals("Maria", result.getFirstName());
        assertEquals("Garcia", result.getLastName());
    }
    
    @Test
    void testToEntity_NullRequest() {
        Customer result = mapper.toEntity(null);
        assertNull(result);
    }
    
    @Test
    void testToResponse_Success() {
        CustomerResponse result = mapper.toResponse(customer);
        
        assertNotNull(result);
        assertEquals("1", result.getId());
        assertEquals("Customer", result.getType());
        assertNotNull(result.getHref());
        assertTrue(result.getName().contains("Juan"));
        assertTrue(result.getName().contains("Perez"));
        assertNotNull(result.getEngagedParty());
        assertNotNull(result.getContactMedium());
        assertEquals(1, result.getContactMedium().size());
        assertEquals("juan@example.com", result.getContactMedium().get(0).getEmailAddress());
        assertEquals("Active", result.getStatus());
        assertNotNull(result.getCreationDate());
        assertNotNull(result.getUpdateDate());
    }
    
    @Test
    void testToResponse_NullCustomer() {
        CustomerResponse result = mapper.toResponse(null);
        assertNull(result);
    }
    
    @Test
    void testUpdateEntityFromRequest_Success() {
        ContactMedium updatedContact = new ContactMedium();
        updatedContact.setEmailAddress("carlos@example.com");
        updatedContact.setPhoneNumber("9999999999");
        updatedContact.setMobileNumber("8888888888");
        
        request.setFirstName("Carlos");
        request.setLastName("Garcia");
        request.setContactMedium(Arrays.asList(updatedContact));
        
        mapper.updateEntityFromRequest(request, customer);
        
        assertEquals("Carlos", customer.getFirstName());
        assertEquals("Garcia", customer.getLastName());
        assertEquals("carlos@example.com", customer.getEmail());
        assertEquals("9999999999", customer.getPhoneNumber());
        assertEquals("8888888888", customer.getMobileNumber());
    }
    
    @Test
    void testUpdateEntityFromRequest_NullRequest() {
        Customer original = new Customer();
        original.setFirstName("Original");
        
        mapper.updateEntityFromRequest(null, original);
        
        assertEquals("Original", original.getFirstName());
    }
    
    @Test
    void testUpdateEntityFromRequest_NullCustomer() {
        mapper.updateEntityFromRequest(request, null);
        // Should not throw an exception
    }
    
    @Test
    void testUpdateEntityFromPatchRequest_PartialUpdate() {
        // Only update firstName
        patchRequest.setFirstName("Juan Carlos");
        patchRequest.setLastName(null);
        patchRequest.setContactMedium(null);
        
        mapper.updateEntityFromPatchRequest(patchRequest, customer);
        
        assertEquals("Juan Carlos", customer.getFirstName());
        assertEquals("Perez", customer.getLastName()); // Should remain unchanged
        // Email should remain unchanged
        assertEquals("juan@example.com", customer.getEmail());
    }
    
    @Test
    void testUpdateEntityFromPatchRequest_UpdateLastNameOnly() {
        // Only update lastName
        patchRequest.setFirstName(null);
        patchRequest.setLastName("Garcia");
        patchRequest.setContactMedium(null);
        
        mapper.updateEntityFromPatchRequest(patchRequest, customer);
        
        assertEquals("Juan", customer.getFirstName()); // Should remain unchanged
        assertEquals("Garcia", customer.getLastName());
    }
    
    @Test
    void testUpdateEntityFromPatchRequest_UpdateContactOnly() {
        ContactMedium newContact = new ContactMedium();
        newContact.setEmailAddress("newemail@example.com");
        newContact.setPhoneNumber("7777777777");
        
        patchRequest.setFirstName(null);
        patchRequest.setContactMedium(Arrays.asList(newContact));
        
        mapper.updateEntityFromPatchRequest(patchRequest, customer);
        
        // Name should remain unchanged
        assertEquals("Juan", customer.getFirstName());
        // Email should be updated
        assertEquals("newemail@example.com", customer.getEmail());
        assertEquals("7777777777", customer.getPhoneNumber());
    }
    
    @Test
    void testUpdateEntityFromPatchRequest_NullRequest() {
        Customer original = new Customer();
        original.setFirstName("Original");
        
        mapper.updateEntityFromPatchRequest(null, original);
        
        assertEquals("Original", original.getFirstName());
    }
}
