package com.tmforum.openapi.integration;

import com.tmforum.openapi.dto.ContactMedium;
import com.tmforum.openapi.dto.CustomerRequest;
import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.dto.LoginResponse;
import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.model.Customer;
import com.tmforum.openapi.repository.ApplicationRepository;
import com.tmforum.openapi.repository.CustomerRepository;
import com.tmforum.openapi.config.SecurityConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests that use MongoDB locally (without Docker/Testcontainers).
 * These tests require that MongoDB is running locally on localhost:27017
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.data.mongodb.host=localhost",
    "spring.data.mongodb.port=27017",
    "spring.data.mongodb.database=openapidb_test"
})
class CustomerControllerIntegrationTestWithoutDocker {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private String authToken;
    private Customer customerTest;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean database
        customerRepository.deleteAll();
        applicationRepository.deleteAll();
        
        // Create test application
        Application application = new Application();
        application.setClientId("app-test");
        application.setClientSecret(passwordEncoder.encode("secret-test"));
        application.setName("Test Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE, SecurityConstants.SCOPE_CUSTOMERS_DELETE, SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
        application.setStatus(true);
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        applicationRepository.save(application);
        
        // Get authentication token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setClientId("app-test");
        loginRequest.setClientSecret("secret-test");
        
        String loginResponse = mockMvc.perform(post("/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginRequest))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        LoginResponse response = objectMapper.readValue(loginResponse, LoginResponse.class);
        authToken = response.getToken();
        
        // Create test customer
        customerTest = new Customer();
        customerTest.setFirstName("Test");
        customerTest.setLastName("User");
        customerTest.setEmail("test@example.com");
        customerTest.setPhoneNumber("1234567890");
        customerTest.setAddress("Test Street 123");
        customerTest.setCreationDate(LocalDateTime.now());
        customerTest.setUpdateDate(LocalDateTime.now());
        @SuppressWarnings("null")
        Customer savedCustomer = customerRepository.save(customerTest);
        customerTest = savedCustomer;
    }
    
    @Test
    void testGetAllCustomers_WithPagination() throws Exception {
        mockMvc.perform(get("/customers")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void testGetCustomerById_Success() throws Exception {
        mockMvc.perform(get("/customers/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerTest.getId()))
                .andExpect(jsonPath("$.firstName").value("Test"));
    }
    
    @Test
    void testGetCustomerById_NotFound() throws Exception {
        mockMvc.perform(get("/customers/999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateCustomer_Success() throws Exception {
        CustomerRequest newRequest = new CustomerRequest();
        newRequest.setFirstName("New");
        newRequest.setLastName("Customer");
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("new@example.com");
        contactMedium.setPhoneNumber("9876543210");
        contactMedium.setStreet1("New Street 456");
        newRequest.setContactMedium(Arrays.asList(contactMedium));

        mockMvc.perform(post("/customers")
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(newRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.updateDate").exists());
    }
    
    @Test
    void testCreateCustomer_EmailDuplicate() throws Exception {
        CustomerRequest duplicateCustomer = new CustomerRequest();
        duplicateCustomer.setFirstName("Other");
        duplicateCustomer.setLastName("Customer");
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("test@example.com");
        contactMedium.setPhoneNumber("1111111111");
        duplicateCustomer.setContactMedium(Arrays.asList(contactMedium));

        mockMvc.perform(post("/customers")
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(duplicateCustomer))))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testUpdateCustomer_Success() throws Exception {
        CustomerRequest updatedRequest = new CustomerRequest();
        updatedRequest.setFirstName("Updated");
        updatedRequest.setLastName(customerTest.getLastName());
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("updated@example.com");
        contactMedium.setPhoneNumber("9999999999");
        contactMedium.setMobileNumber(customerTest.getMobileNumber());
        contactMedium.setStreet1(customerTest.getAddress());
        updatedRequest.setContactMedium(Arrays.asList(contactMedium));
        
        mockMvc.perform(put("/customers/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(updatedRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.phoneNumber").value("9999999999"))
                .andExpect(jsonPath("$.updateDate").exists());
    }
    
    @Test
    void testDeleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/customers/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void testsearchCustomers_WithPagination() throws Exception {
        mockMvc.perform(get("/customers/search")
                .header("Authorization", "Bearer " + authToken)
                .param("name", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void testGetAllCustomers_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }
}

