package com.tmforum.openapi.controller.v1;

import com.tmforum.openapi.dto.CustomerRequestV1;
import com.tmforum.openapi.dto.CustomerResponseV1;
import com.tmforum.openapi.dto.PageResponse;
import com.tmforum.openapi.dto.ContactMedium;
import com.tmforum.openapi.model.DocumentType;
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
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerV1ControllerTest {
    
    @Mock
    private CustomerService customerService;
    
    @InjectMocks
    private CustomerV1Controller customerV1Controller;
    
    private CustomerRequestV1 customerRequest;
    private CustomerResponseV1 customerResponse;
    
    @BeforeEach
    void setUp() {
        customerRequest = new CustomerRequestV1();
        customerRequest.setFirstName("Juan");
        customerRequest.setLastName("Perez");
        customerRequest.setEmail("juan@example.com");
        customerRequest.setPhoneNumber("1234567890");
        customerRequest.setMobileNumber("0987654321");
        customerRequest.setAddress("Street 123");
        
        customerResponse = new CustomerResponseV1();
        customerResponse.setId("1");
        customerResponse.setFirstName("Juan");
        customerResponse.setLastName("Perez");
        customerResponse.setEmail("juan@example.com");
        customerResponse.setPhoneNumber("1234567890");
        customerResponse.setMobileNumber("0987654321");
        customerResponse.setAddress("Street 123");
        customerResponse.setCreationDate(LocalDateTime.now());
        customerResponse.setUpdateDate(LocalDateTime.now());
    }
    
    @Test
    void testGetAllCustomers_Success() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.getAllCustomersV1(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.getAllCustomers(0, 10, "creationDate", "DESC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PageResponse<CustomerResponseV1> body = Objects.requireNonNull(response.getBody());
        assertEquals(1, body.getContent().size());
        verify(customerService, times(1)).getAllCustomersV1(any(Pageable.class));
    }
    
    @Test
    void testGetAllCustomers_WithSortASC() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.getAllCustomersV1(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.getAllCustomers(0, 10, "firstName", "ASC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).getAllCustomersV1(any(Pageable.class));
    }
    
    @Test
    void testGetAllCustomers_WithDefaultParameters() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.getAllCustomersV1(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.getAllCustomers(0, 10, "creationDate", "DESC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).getAllCustomersV1(any(Pageable.class));
    }
    
    @Test
    void testGetAllCustomers_WithLowerCaseDirection() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.getAllCustomersV1(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.getAllCustomers(0, 10, "firstName", "asc");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).getAllCustomersV1(any(Pageable.class));
    }
    
    @Test
    void testGetCustomerById_Success() {
        when(customerService.getCustomerByIdV1("1")).thenReturn(Optional.of(customerResponse));
        
        ResponseEntity<CustomerResponseV1> response = customerV1Controller.getCustomerById("1");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CustomerResponseV1 body = Objects.requireNonNull(response.getBody());
        assertEquals("1", body.getId());
        assertEquals("Juan", body.getFirstName());
        assertEquals("Perez", body.getLastName());
        verify(customerService, times(1)).getCustomerByIdV1("1");
    }
    
    @Test
    void testGetCustomerById_NotFound() {
        when(customerService.getCustomerByIdV1("999")).thenReturn(Optional.empty());
        
        ResponseEntity<CustomerResponseV1> response = customerV1Controller.getCustomerById("999");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerService, times(1)).getCustomerByIdV1("999");
    }
    
    @Test
    void testCreateCustomer_Success() {
        when(customerService.createCustomerV1(any(CustomerRequestV1.class))).thenReturn(customerResponse);
        
        ResponseEntity<CustomerResponseV1> response = customerV1Controller.createCustomer(customerRequest);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        CustomerResponseV1 body = Objects.requireNonNull(response.getBody());
        assertEquals("1", body.getId());
        assertEquals("Juan", body.getFirstName());
        assertEquals("Perez", body.getLastName());
        verify(customerService, times(1)).createCustomerV1(customerRequest);
    }
    
    @Test
    void testUpdateCustomer_Success() {
        when(customerService.updateCustomerV1(eq("1"), any(CustomerRequestV1.class)))
            .thenReturn(customerResponse);
        
        ResponseEntity<CustomerResponseV1> response = customerV1Controller.updateCustomer("1", customerRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        CustomerResponseV1 body = Objects.requireNonNull(response.getBody());
        assertEquals("1", body.getId());
        verify(customerService, times(1)).updateCustomerV1("1", customerRequest);
    }
    
    @Test
    void testUpdateCustomer_NotFound() {
        when(customerService.updateCustomerV1(eq("999"), any(CustomerRequestV1.class)))
            .thenThrow(new RuntimeException("Customer not found with ID: 999"));
        
        ResponseEntity<CustomerResponseV1> response = customerV1Controller.updateCustomer("999", customerRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerService, times(1)).updateCustomerV1("999", customerRequest);
    }
    
    @Test
    void testDeleteCustomer_Success() {
        doNothing().when(customerService).deleteCustomer("1");
        
        ResponseEntity<Void> response = customerV1Controller.deleteCustomer("1");
        
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(customerService, times(1)).deleteCustomer("1");
    }
    
    @Test
    void testDeleteCustomer_NotFound() {
        doThrow(new RuntimeException("Customer not found with ID: 999"))
            .when(customerService).deleteCustomer("999");
        
        ResponseEntity<Void> response = customerV1Controller.deleteCustomer("999");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(customerService, times(1)).deleteCustomer("999");
    }
    
    @Test
    void testsearchCustomers_Success() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.searchCustomersV1(eq("Juan"), any(Pageable.class)))
            .thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.searchCustomers("Juan", 0, 10, "firstName", "ASC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PageResponse<CustomerResponseV1> body = Objects.requireNonNull(response.getBody());
        assertEquals(1, body.getContent().size());
        verify(customerService, times(1)).searchCustomersV1(eq("Juan"), any(Pageable.class));
    }
    
    @Test
    void testsearchCustomers_WithDESC() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.searchCustomersV1(eq("Juan"), any(Pageable.class)))
            .thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.searchCustomers("Juan", 0, 10, "lastName", "DESC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(customerService, times(1)).searchCustomersV1(eq("Juan"), any(Pageable.class));
    }
    
    @Test
    void testsearchCustomers_WithDefaultParameters() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.searchCustomersV1(eq("Perez"), any(Pageable.class)))
            .thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.searchCustomers("Perez", 0, 10, "firstName", "ASC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).searchCustomersV1(eq("Perez"), any(Pageable.class));
    }
    
    @Test
    void testsearchCustomers_WithLowerCaseDirection() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 0, 10, 1);
        
        when(customerService.searchCustomersV1(eq("Juan"), any(Pageable.class)))
            .thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.searchCustomers("Juan", 0, 10, "firstName", "desc");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customerService, times(1)).searchCustomersV1(eq("Juan"), any(Pageable.class));
    }
    
    @Test
    void testGetAllCustomers_WithDifferentPageAndSize() {
        PageResponse<CustomerResponseV1> pageResponse = PageResponse.of(
            Arrays.asList(customerResponse), 1, 5, 10);
        
        when(customerService.getAllCustomersV1(any(Pageable.class))).thenReturn(pageResponse);
        
        ResponseEntity<PageResponse<CustomerResponseV1>> response = 
            customerV1Controller.getAllCustomers(1, 5, "firstName", "ASC");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        PageResponse<CustomerResponseV1> body = Objects.requireNonNull(response.getBody());
        assertEquals(1, body.getPage());
        assertEquals(5, body.getSize());
        verify(customerService, times(1)).getAllCustomersV1(any(Pageable.class));
    }
}

