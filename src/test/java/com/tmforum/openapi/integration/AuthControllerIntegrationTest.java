package com.tmforum.openapi.integration;

import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import com.tmforum.openapi.config.SecurityConstants;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true", disabledReason = "Docker not available. Run with -DDOCKER_AVAILABLE=true if Docker is available")
class AuthControllerIntegrationTest {
    
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
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
    }
    
    @Test
    void testLogin_Successful() throws Exception {
        // Given
        Application application = new Application();
        application.setClientId("app-test");
        application.setClientSecret(passwordEncoder.encode("secret-test"));
        application.setName("Test Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        application.setStatus(true);
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        applicationRepository.save(application);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setClientId("app-test");
        loginRequest.setClientSecret("secret-test");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginRequest))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.clientId").value("app-test"))
                .andExpect(jsonPath("$.scopes").isArray());
    }
    
    @Test
    void testLogin_ClientIdNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setClientId("app-nonexistent");
        loginRequest.setClientSecret("secret");
        
        mockMvc.perform(post("/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginRequest))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("ClientId not found or inactive"));
    }
    
    @Test
    void testLogin_ClientSecretIncorrect() throws Exception {
        // Given
        Application application = new Application();
        application.setClientId("app-test");
        application.setClientSecret(passwordEncoder.encode("secret-correct"));
        application.setName("Test Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        application.setStatus(true);
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        applicationRepository.save(application);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setClientId("app-test");
        loginRequest.setClientSecret("secret-incorrect");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginRequest))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Incorrect ClientSecret"));
    }
    
    @Test
    void testLogin_ApplicationInactive() throws Exception {
        // Given
        Application application = new Application();
        application.setClientId("app-inactive");
        application.setClientSecret(passwordEncoder.encode("secret"));
        application.setName("Inactive Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        application.setStatus(false); // Inactive
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        applicationRepository.save(application);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setClientId("app-inactive");
        loginRequest.setClientSecret("secret");
        
        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(loginRequest))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("ClientId not found or inactive"));
    }
    
    @Test
    void testValidateToken_Valid() throws Exception {
        // Given - Create application and get token
        Application application = new Application();
        application.setClientId("app-test");
        application.setClientSecret(passwordEncoder.encode("secret-test"));
        application.setName("Test Application");
        application.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        application.setStatus(true);
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        applicationRepository.save(application);
        
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
        
        String token = objectMapper.readTree(loginResponse).get("token").asText();
        
        // When & Then
        mockMvc.perform(get("/auth/validate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }
}

