package com.tmforum.openapi.integration;

import com.tmforum.openapi.dto.*;
import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.model.Customer;
import com.tmforum.openapi.repository.ApplicationRepository;
import com.tmforum.openapi.config.SecurityConstants;
import com.tmforum.openapi.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true", disabledReason = "Docker not available. Run with -DDOCKER_AVAILABLE=true if Docker is available")
class CustomerControllerIntegrationTest {
    
    @Container
    @SuppressWarnings("resource")
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withReuse(true);
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (mongoDBContainer.isRunning()) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        }
    }
    
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
    void testListCustomer_WithOffsetAndLimit() throws Exception {
        mockMvc.perform(get("/customer")
                .header("Authorization", "Bearer " + authToken)
                .param("offset", "0")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    
    @Test
    void testRetrieveCustomer_Success() throws Exception {
        mockMvc.perform(get("/customer/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerTest.getId()))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.@type").value("Customer"))
                .andExpect(jsonPath("$.engagedParty").exists())
                .andExpect(jsonPath("$.contactMedium").isArray());
    }
    
    @Test
    void testRetrieveCustomer_NotFound() throws Exception {
        mockMvc.perform(get("/customer/999")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testCreateCustomer_Success() throws Exception {
        // Create TMF629 CustomerRequest
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("New Customer");
        engagedParty.setReferredType("Individual");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setType("EmailContactMedium");
        contactMedium.setEmailAddress("new@example.com");
        contactMedium.setPhoneNumber("9876543210");
        contactMedium.setStreet1("New Street 456");
        
        CustomerRequest newRequest = new CustomerRequest();
        newRequest.setFirstName("New");
        newRequest.setLastName("Customer");
        newRequest.setEngagedParty(engagedParty);
        newRequest.setContactMedium(Arrays.asList(contactMedium));
        newRequest.setStatus("Active");
        
        mockMvc.perform(post("/customer")
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(newRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Customer"))
                .andExpect(jsonPath("$.@type").value("Customer"))
                .andExpect(jsonPath("$.contactMedium[0].emailAddress").value("new@example.com"))
                .andExpect(jsonPath("$.creationDate").exists())
                .andExpect(jsonPath("$.updateDate").exists());
    }
    
    @Test
    void testCreateCustomer_EmailDuplicate() throws Exception {
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Duplicate Customer");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("test@example.com"); // Duplicate email
        
        CustomerRequest duplicateCustomer = new CustomerRequest();
        duplicateCustomer.setFirstName("Duplicate");
        duplicateCustomer.setLastName("Customer");
        duplicateCustomer.setEngagedParty(engagedParty);
        duplicateCustomer.setContactMedium(Arrays.asList(contactMedium));
        
        mockMvc.perform(post("/customer")
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(duplicateCustomer))))
                .andExpect(status().isConflict());
    }
    
    @Test
    void testCreateCustomer_MissingEngagedParty() throws Exception {
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("test2@example.com");
        
        CustomerRequest invalidRequest = new CustomerRequest();
        invalidRequest.setFirstName("Invalid");
        invalidRequest.setLastName("Customer");
        invalidRequest.setContactMedium(Arrays.asList(contactMedium));
        // Missing engagedParty - should fail validation
        
        mockMvc.perform(post("/customer")
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest))))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testPatchCustomer_Success() throws Exception {
        ContactMedium updatedContact = new ContactMedium();
        updatedContact.setEmailAddress("updated@example.com");
        updatedContact.setPhoneNumber("9999999999");
        
        CustomerPatchRequest patchRequest = new CustomerPatchRequest();
        patchRequest.setFirstName("Updated");
        patchRequest.setLastName("Customer");
        patchRequest.setContactMedium(Arrays.asList(updatedContact));
        
        mockMvc.perform(patch("/customer/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(patchRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Customer"))
                .andExpect(jsonPath("$.contactMedium[0].emailAddress").value("updated@example.com"))
                .andExpect(jsonPath("$.updateDate").exists());
    }
    
    @Test
    void testPatchCustomer_PartialUpdate() throws Exception {
        // Only update firstName, leave contactMedium unchanged
        CustomerPatchRequest patchRequest = new CustomerPatchRequest();
        patchRequest.setFirstName("Partially");
        patchRequest.setLastName("Updated");
        
        mockMvc.perform(patch("/customer/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(patchRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partially Updated"));
    }
    
    @Test
    void testDeleteCustomer_Success() throws Exception {
        mockMvc.perform(delete("/customer/" + customerTest.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    void testListCustomer_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/customer"))
                .andExpect(status().isUnauthorized());
    }
}
