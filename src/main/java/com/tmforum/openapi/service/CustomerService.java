package com.tmforum.openapi.service;

import com.tmforum.openapi.dto.CustomerRequest;
import com.tmforum.openapi.dto.CustomerRequestV1;
import com.tmforum.openapi.dto.CustomerPatchRequest;
import com.tmforum.openapi.dto.CustomerResponse;
import com.tmforum.openapi.dto.CustomerResponseV1;
import com.tmforum.openapi.dto.PageResponse;
import com.tmforum.openapi.mapper.CustomerMapper;
import com.tmforum.openapi.model.Customer;
import com.tmforum.openapi.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private CustomerMapper customerMapper;
    
    // Get all customers with pagination
    @Cacheable(value = "customersList", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#pageable.sort != null ? #pageable.sort.toString() : 'default')")
    public PageResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Pageable nonNullPageable = Objects.requireNonNull(pageable);
        Page<Customer> page = customerRepository.findAll(nonNullPageable);
        List<CustomerResponse> content = page.getContent().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.of(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
    
    // Get a customer by ID
    @Cacheable(value = "customers", key = "#id")
    public Optional<CustomerResponse> getCustomerById(String id) {
        String nonNullId = Objects.requireNonNull(id);
        return customerRepository.findById(nonNullId)
                .map(customerMapper::toResponse);
    }
    
    // Create a new customer
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Extract email from contactMedium for validation
        String email = null;
        if (request.getContactMedium() != null) {
            email = request.getContactMedium().stream()
                    .filter(cm -> cm.getEmailAddress() != null && !cm.getEmailAddress().isEmpty())
                    .map(cm -> cm.getEmailAddress())
                    .findFirst()
                    .orElse(null);
        }
        
        // Check if a customer with that email already exists
        if (email != null && customerRepository.existsByEmail(email)) {
            throw new RuntimeException("A customer with the email already exists: " + email);
        }
        
        // Convert DTO to Entity (dates are generated automatically)
        Customer customer = customerMapper.toEntity(request);
        @SuppressWarnings("null")
        Customer customerGuardado = customerRepository.save(customer);
        
        return customerMapper.toResponse(customerGuardado);
    }
    
    // Update a customer (PUT - full update)
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public CustomerResponse updateCustomer(String id, CustomerRequest request) {
        String nonNullId = Objects.requireNonNull(id);
        return customerRepository.findById(nonNullId)
                .map(customer -> {
                    // Extract email from contactMedium for validation
                    String email = null;
                    if (request.getContactMedium() != null) {
                        email = request.getContactMedium().stream()
                                .filter(cm -> cm.getEmailAddress() != null && !cm.getEmailAddress().isEmpty())
                                .map(cm -> cm.getEmailAddress())
                                .findFirst()
                                .orElse(null);
                    }
                    
                    // Check if email is being changed and if it already exists
                    if (email != null && !customer.getEmail().equals(email) 
                        && customerRepository.existsByEmail(email)) {
                        throw new RuntimeException("A customer with the email already exists: " + email);
                    }
                    
                    // Update fields (updateDate is updated automatically)
                    customerMapper.updateEntityFromRequest(request, customer);
                    Customer savedCustomer = customerRepository.save(customer);
                    Customer updatedCustomer = Objects.requireNonNull(savedCustomer);
                    
                    return customerMapper.toResponse(updatedCustomer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + nonNullId));
    }
    
    // Patch a customer (PATCH - partial update)
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public CustomerResponse patchCustomer(String id, CustomerPatchRequest request) {
        String nonNullId = Objects.requireNonNull(id);
        return customerRepository.findById(nonNullId)
                .map(customer -> {
                    // Extract email from contactMedium for validation if provided
                    if (request.getContactMedium() != null) {
                        String email = request.getContactMedium().stream()
                                .filter(cm -> cm.getEmailAddress() != null && !cm.getEmailAddress().isEmpty())
                                .map(cm -> cm.getEmailAddress())
                                .findFirst()
                                .orElse(null);
                        
                        // Check if email is being changed and if it already exists
                        if (email != null && !customer.getEmail().equals(email) 
                            && customerRepository.existsByEmail(email)) {
                            throw new RuntimeException("A customer with the email already exists: " + email);
                        }
                    }
                    
                    // Update only provided fields (updateDate is updated automatically)
                    customerMapper.updateEntityFromPatchRequest(request, customer);
                    Customer savedCustomer = customerRepository.save(customer);
                    Customer updatedCustomer = Objects.requireNonNull(savedCustomer);
                    
                    return customerMapper.toResponse(updatedCustomer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + nonNullId));
    }
    
    // Delete a customer
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public void deleteCustomer(String id) {
        String nonNullId = Objects.requireNonNull(id);
        if (!customerRepository.existsById(nonNullId)) {
            throw new RuntimeException("Customer not found with ID: " + nonNullId);
        }
        customerRepository.deleteById(nonNullId);
    }
    
    // Search customers by name or surname with pagination
    @Cacheable(value = "customersList", key = "'search_' + #search + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageResponse<CustomerResponse> searchCustomers(String search, Pageable pageable) {
        Pageable nonNullPageable = Objects.requireNonNull(pageable);
        Page<Customer> page = customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            search, search, nonNullPageable);
        List<CustomerResponse> content = page.getContent().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.of(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
    
    // Get customer by email
    public Optional<CustomerResponse> getCustomerByEmail(String email) {
        String nonNullEmail = Objects.requireNonNull(email);
        return customerRepository.findByEmail(nonNullEmail)
                .map(customerMapper::toResponse);
    }
    
    // Methods for CustomerRequestV1 (legacy v1 API)
    
    // Get all customers with pagination (v1)
    @Cacheable(value = "customersList", key = "'v1_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + (#pageable.sort != null ? #pageable.sort.toString() : 'default')")
    public PageResponse<CustomerResponseV1> getAllCustomersV1(Pageable pageable) {
        Pageable nonNullPageable = Objects.requireNonNull(pageable);
        Page<Customer> page = customerRepository.findAll(nonNullPageable);
        List<CustomerResponseV1> content = page.getContent().stream()
                .map(customerMapper::toResponseV1)
                .collect(Collectors.toList());
        
        return PageResponse.of(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
    
    // Get a customer by ID (v1)
    @Cacheable(value = "customers", key = "'v1_' + #id")
    public Optional<CustomerResponseV1> getCustomerByIdV1(String id) {
        String nonNullId = Objects.requireNonNull(id);
        return customerRepository.findById(nonNullId)
                .map(customerMapper::toResponseV1);
    }
    
    // Create a new customer (v1)
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public CustomerResponseV1 createCustomerV1(CustomerRequestV1 request) {
        // Check if a customer with that email already exists
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("A customer with the email already exists: " + request.getEmail());
        }
        
        // Convert DTO to Entity (dates are generated automatically)
        Customer customer = customerMapper.toEntityV1(request);
        @SuppressWarnings("null")
        Customer savedCustomer = customerRepository.save(customer);
        
        return customerMapper.toResponseV1(savedCustomer);
    }
    
    // Update a customer (v1 - PUT - full update)
    @CacheEvict(value = {"customers", "customersList"}, allEntries = true)
    public CustomerResponseV1 updateCustomerV1(String id, CustomerRequestV1 request) {
        String nonNullId = Objects.requireNonNull(id);
        return customerRepository.findById(nonNullId)
                .map(customer -> {
                    // Check if email is being changed and if it already exists
                    if (request.getEmail() != null && !customer.getEmail().equals(request.getEmail()) 
                        && customerRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("A customer with the email already exists: " + request.getEmail());
                    }
                    
                    // Update fields (updateDate is updated automatically)
                    customerMapper.updateEntityFromRequestV1(request, customer);
                    Customer savedCustomer = customerRepository.save(customer);
                    Customer updatedCustomer = Objects.requireNonNull(savedCustomer);
                    
                    return customerMapper.toResponseV1(updatedCustomer);
                })
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + nonNullId));
    }
    
    // Search customers by name or surname with pagination (v1)
    @Cacheable(value = "customersList", key = "'v1_search_' + #search + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageResponse<CustomerResponseV1> searchCustomersV1(String search, Pageable pageable) {
        Pageable nonNullPageable = Objects.requireNonNull(pageable);
        Page<Customer> page = customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            search, search, nonNullPageable);
        List<CustomerResponseV1> content = page.getContent().stream()
                .map(customerMapper::toResponseV1)
                .collect(Collectors.toList());
        
        return PageResponse.of(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements()
        );
    }
}

