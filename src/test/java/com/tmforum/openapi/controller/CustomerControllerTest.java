package com.tmforum.openapi.controller;

import com.tmforum.openapi.dto.*;
import com.tmforum.openapi.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {
    
    @Mock
    private CustomerService customerService;
    
    @InjectMocks
    private CustomerController customerController;
    
    private CustomerRequest customerRequest;
    private CustomerPatchRequest customerPatchRequest;
    private CustomerResponse customerResponse;
    
    @BeforeEach
    void setUp() {
        // Create PartyRef for engagedParty
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
        
        // Create CustomerRequest (Customer_FVO)
        customerRequest = new CustomerRequest();
        customerRequest.setFirstName("Juan");
        customerRequest.setLastName("Perez");
        customerRequest.setEngagedParty(engagedParty);
        customerRequest.setContactMedium(Arrays.asList(contactMedium));
        customerRequest.setStatus("Active");
        
        // Create CustomerPatchRequest (Customer_MVO)
        customerPatchRequest = new CustomerPatchRequest();
        customerPatchRequest.setFirstName("Juan Carlos");
        customerPatchRequest.setLastName("Perez");
        customerPatchRequest.setContactMedium(Arrays.asList(contactMedium));
        
        // Create CustomerResponse
        customerResponse = new CustomerResponse();
        customerResponse.setId("1");
        customerResponse.setName("Juan Perez");
        customerResponse.setHref("/api/customer/1");
        customerResponse.setEngagedParty(engagedParty);
        customerResponse.setContactMedium(Arrays.asList(contactMedium));
        customerResponse.setStatus("Active");
        customerResponse.setCreationDate(LocalDateTime.now());
        customerResponse.setUpdateDate(LocalDateTime.now());
    }
    
    @Test
    void testListCustomer_Success() {
        PageResponse<CustomerResponse> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponse>> response = 
            customerController.listCustomer(null, 0, 10, "creationDate", "DESC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PageResponse<CustomerResponse> body = Objects.requireNonNull(response.getBody());
        assertEquals(1, body.getContent().size());
        verify(customerService, times(1)).getAllCustomers(any(Pageable.class));
    }
    
    @Test
    void testListCustomer_WithOffsetAndLimit() {
        PageResponse<CustomerResponse> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 20, 1);
        
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponse>> response = 
            customerController.listCustomer(null, 20, 20, "name", "ASC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).getAllCustomers(any(Pageable.class));
    }
    
    @Test
    void testRetrieveCustomer_Success() {
        when(customerService.getCustomerById("1")).thenReturn(Optional.of(customerResponse));
        
        ResponseEntity<CustomerResponse> response = customerController.retrieveCustomer("1", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CustomerResponse body = Objects.requireNonNull(response.getBody());
        assertEquals("1", body.getId());
        assertEquals("Juan Perez", body.getName());
    }
    
    @Test
    void testRetrieveCustomer_NotFound() {
        when(customerService.getCustomerById("999")).thenReturn(Optional.empty());
        
        ResponseEntity<CustomerResponse> response = customerController.retrieveCustomer("999", null);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void testCreateCustomer_Success() {
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(customerResponse);
        
        ResponseEntity<?> response = customerController.createCustomer(customerRequest);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).createCustomer(customerRequest);
    }
    
    @Test
    void testCreateCustomer_EmailDuplicate() {
        when(customerService.createCustomer(any(CustomerRequest.class)))
            .thenThrow(new RuntimeException("A customer with the email juan@example.com already exists"));
        
        ResponseEntity<?> response = customerController.createCustomer(customerRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
    
    @Test
    void testCreateCustomer_InvalidData() {
        when(customerService.createCustomer(any(CustomerRequest.class)))
            .thenThrow(new RuntimeException("Invalid data"));
        
        ResponseEntity<?> response = customerController.createCustomer(customerRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    void testPatchCustomer_Success() {
        when(customerService.patchCustomer(eq("1"), any(CustomerPatchRequest.class)))
            .thenReturn(customerResponse);
        
        ResponseEntity<?> response = customerController.patchCustomer("1", customerPatchRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).patchCustomer("1", customerPatchRequest);
    }
    
    @Test
    void testPatchCustomer_NotFound() {
        when(customerService.patchCustomer(eq("999"), any(CustomerPatchRequest.class)))
            .thenThrow(new RuntimeException("Customer not found with ID: 999"));
        
        ResponseEntity<?> response = customerController.patchCustomer("999", customerPatchRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    void testPatchCustomer_EmailConflict() {
        when(customerService.patchCustomer(eq("1"), any(CustomerPatchRequest.class)))
            .thenThrow(new RuntimeException("A customer with the email already exists: juan@example.com"));
        
        ResponseEntity<?> response = customerController.patchCustomer("1", customerPatchRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
    
    @Test
    void testDeleteCustomer_Success() {
        doNothing().when(customerService).deleteCustomer("1");
        
        ResponseEntity<?> response = customerController.deleteCustomer("1");
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerService, times(1)).deleteCustomer("1");
    }
    
    @Test
    void testDeleteCustomer_NotFound() {
        doThrow(new RuntimeException("Customer not found with ID: 999"))
            .when(customerService).deleteCustomer("999");
        
        ResponseEntity<?> response = customerController.deleteCustomer("999");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
