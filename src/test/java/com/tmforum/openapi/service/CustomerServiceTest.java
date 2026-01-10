package com.tmforum.openapi.service;

import com.tmforum.openapi.dto.*;
import com.tmforum.openapi.mapper.CustomerMapper;
import com.tmforum.openapi.model.Customer;
import com.tmforum.openapi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private CustomerMapper customerMapper;
    
    @InjectMocks
    private CustomerService customerService;
    
    private Customer customer;
    private CustomerRequest customerRequest;
    private CustomerPatchRequest customerPatchRequest;
    private CustomerResponse customerResponse;
    
    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId("1");
        customer.setFirstName("Juan");
        customer.setLastName("Perez");
        customer.setEmail("juan.perez@example.com");
        customer.setPhoneNumber("1234567890");
        customer.setMobileNumber("0987654321");
        customer.setAddress("Main Street 123");
        customer.setCreationDate(LocalDateTime.now());
        customer.setUpdateDate(LocalDateTime.now());
        
        // Create TMF629 CustomerRequest
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Juan Perez");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("juan.perez@example.com");
        contactMedium.setPhoneNumber("1234567890");
        contactMedium.setMobileNumber("0987654321");
        
        customerRequest = new CustomerRequest();
        customerRequest.setFirstName("Juan");
        customerRequest.setLastName("Perez");
        customerRequest.setEngagedParty(engagedParty);
        customerRequest.setContactMedium(Arrays.asList(contactMedium));
        
        // Create TMF629 CustomerPatchRequest
        customerPatchRequest = new CustomerPatchRequest();
        customerPatchRequest.setFirstName("Juan Carlos");
        customerPatchRequest.setLastName("Perez");
        
        ContactMedium updatedContact = new ContactMedium();
        updatedContact.setEmailAddress("juan.carlos@example.com");
        customerPatchRequest.setContactMedium(Arrays.asList(updatedContact));
        
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
    void testGetCustomerById_Exists() {
        // Given
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);
        
        // When
        Optional<CustomerResponse> result = customerService.getCustomerById("1");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        verify(customerRepository, times(1)).findById("1");
        verify(customerMapper, times(1)).toResponse(customer);
    }
    
    @Test
    void testGetCustomerById_NotFound() {
        // Given
        when(customerRepository.findById("999")).thenReturn(Optional.empty());
        
        // When
        Optional<CustomerResponse> result = customerService.getCustomerById("999");
        
        // Then
        assertFalse(result.isPresent());
        verify(customerRepository, times(1)).findById("999");
        verify(customerMapper, never()).toResponse(any());
    }
    
    @Test
    void testCreateCustomer_Success() {
        // Given
        Customer newCustomer = new Customer();
        newCustomer.setId("2");
        newCustomer.setEmail("maria.garcia@example.com");
        
        CustomerResponse newResponse = new CustomerResponse();
        newResponse.setId("2");
        newResponse.setName("Maria Garcia");
        
        when(customerRepository.existsByEmail("maria.garcia@example.com")).thenReturn(false);
        when(customerMapper.toEntity(any(CustomerRequest.class))).thenReturn(newCustomer);
        when(customerRepository.save(any())).thenReturn(newCustomer);
        when(customerMapper.toResponse(newCustomer)).thenReturn(newResponse);
        
        // Create request with different email
        ContactMedium newContact = new ContactMedium();
        newContact.setEmailAddress("maria.garcia@example.com");
        CustomerRequest newRequest = new CustomerRequest();
        newRequest.setContactMedium(Arrays.asList(newContact));
        
        // When
        CustomerResponse result = customerService.createCustomer(newRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("2", result.getId());
        verify(customerRepository, times(1)).existsByEmail("maria.garcia@example.com");
        verify(customerMapper, times(1)).toEntity(any(CustomerRequest.class));
        verify(customerRepository, times(1)).save(any());
    }
    
    @Test
    void testCreateCustomer_EmailDuplicate() {
        // Given
        when(customerRepository.existsByEmail("juan.perez@example.com")).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.createCustomer(customerRequest);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerRepository, times(1)).existsByEmail("juan.perez@example.com");
        verify(customerRepository, never()).save(any());
    }
    
    @Test
    void testUpdateCustomer_Success() {
        // Given
        CustomerResponse updatedResponse = new CustomerResponse();
        updatedResponse.setId("1");
        updatedResponse.setName("Juan Carlos Perez");
        
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(updatedResponse);
        
        // When
        CustomerResponse result = customerService.updateCustomer("1", customerRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(customerRepository, times(1)).findById("1");
        verify(customerMapper, times(1)).updateEntityFromRequest(customerRequest, customer);
        verify(customerRepository, times(1)).save(any());
    }
    
    @Test
    void testUpdateCustomer_NotFound() {
        // Given
        when(customerRepository.findById("999")).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.updateCustomer("999", customerRequest);
        });
        
        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(customerRepository, times(1)).findById("999");
        verify(customerRepository, never()).save(any());
    }
    
    @Test
    void testPatchCustomer_Success() {
        // Given
        CustomerResponse updatedResponse = new CustomerResponse();
        updatedResponse.setId("1");
        updatedResponse.setName("Juan Carlos Perez");
        
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(updatedResponse);
        
        // When
        CustomerResponse result = customerService.patchCustomer("1", customerPatchRequest);
        
        // Then
        assertNotNull(result);
        assertEquals("1", result.getId());
        verify(customerRepository, times(1)).findById("1");
        verify(customerMapper, times(1)).updateEntityFromPatchRequest(customerPatchRequest, customer);
        verify(customerRepository, times(1)).save(any());
    }
    
    @Test
    void testPatchCustomer_NotFound() {
        // Given
        when(customerRepository.findById("999")).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.patchCustomer("999", customerPatchRequest);
        });
        
        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(customerRepository, times(1)).findById("999");
        verify(customerRepository, never()).save(any());
    }
    
    @Test
    void testPatchCustomer_EmailDuplicate() {
        // Given
        when(customerRepository.findById("1")).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("juan.carlos@example.com")).thenReturn(true);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.patchCustomer("1", customerPatchRequest);
        });
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(customerRepository, times(1)).findById("1");
        verify(customerRepository, times(1)).existsByEmail("juan.carlos@example.com");
        verify(customerRepository, never()).save(any());
    }
    
    @Test
    void testDeleteCustomer_Success() {
        // Given
        when(customerRepository.existsById("1")).thenReturn(true);
        doNothing().when(customerRepository).deleteById("1");
        
        // When
        customerService.deleteCustomer("1");
        
        // Then
        verify(customerRepository, times(1)).existsById("1");
        verify(customerRepository, times(1)).deleteById("1");
    }
    
    @Test
    void testDeleteCustomer_NotFound() {
        // Given
        when(customerRepository.existsById("999")).thenReturn(false);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            customerService.deleteCustomer("999");
        });
        
        assertEquals("Customer not found with ID: 999", exception.getMessage());
        verify(customerRepository, times(1)).existsById("999");
        verify(customerRepository, never()).deleteById(anyString());
    }
    
    @Test
    void testGetAllCustomers_WithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(customer);
        Page<Customer> page = new PageImpl<>(customers, pageable, 1);
        
        when(customerRepository.findAll(pageable)).thenReturn(page);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);
        
        // When
        PageResponse<CustomerResponse> response = customerService.getAllCustomers(pageable);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(1, response.getTotalElements());
        verify(customerRepository, times(1)).findAll(pageable);
        verify(customerMapper, times(1)).toResponse(customer);
    }
    
    @Test
    void testSearchCustomers_Found() {
        // Given
        String search = "Juan";
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(customer);
        Page<Customer> page = new PageImpl<>(customers, pageable, 1);
        
        when(customerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            eq(search), eq(search), eq(pageable))).thenReturn(page);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);
        
        // When
        PageResponse<CustomerResponse> response = customerService.searchCustomers(search, pageable);
        
        // Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        verify(customerRepository, times(1)).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            eq(search), eq(search), eq(pageable));
        verify(customerMapper, times(1)).toResponse(customer);
    }
    
    @Test
    void testGetCustomerByEmail_Exists() {
        // Given
        when(customerRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);
        
        // When
        Optional<CustomerResponse> result = customerService.getCustomerByEmail("juan.perez@example.com");
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("1", result.get().getId());
        verify(customerRepository, times(1)).findByEmail("juan.perez@example.com");
        verify(customerMapper, times(1)).toResponse(customer);
    }
}
