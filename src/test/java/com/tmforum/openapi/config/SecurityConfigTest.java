package com.tmforum.openapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collection;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {
    
    @Autowired
    private SecurityFilterChain securityFilterChain;
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Test
    void testSecurityFilterChainBeanExists() {
        assertNotNull(securityFilterChain);
    }
    
    @Test
    void testCorsConfigurationSourceBeanExists() {
        assertNotNull(corsConfigurationSource);
    }
    
    @Test
    void testCorsConfiguration() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/customers");
        
        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(request);
        
        assertNotNull(configuration);
        
        // Verify specific origins configured
        assertNotNull(configuration.getAllowedOrigins());
        Collection<String> allowedOrigins = Objects.requireNonNull(configuration.getAllowedOrigins());
        assertTrue(allowedOrigins.contains("http://localhost:3000"));
        assertTrue(allowedOrigins.contains("http://localhost:4200"));
        assertTrue(allowedOrigins.contains("http://localhost:8080"));
        assertTrue(allowedOrigins.contains("https://domain.com"));
        
        // Verify allowed HTTP methods
        assertNotNull(configuration.getAllowedMethods());
        Collection<String> allowedMethods = Objects.requireNonNull(configuration.getAllowedMethods());
        assertTrue(allowedMethods.contains("GET"));
        assertTrue(allowedMethods.contains("POST"));
        assertTrue(allowedMethods.contains("PUT"));
        assertTrue(allowedMethods.contains("DELETE"));
        assertTrue(allowedMethods.contains("OPTIONS"));
        assertTrue(allowedMethods.contains("PATCH"));
        
        // Verify allowed headers
        assertNotNull(configuration.getAllowedHeaders());
        Collection<String> allowedHeaders = Objects.requireNonNull(configuration.getAllowedHeaders());
        assertTrue(allowedHeaders.contains("Authorization"));
        assertTrue(allowedHeaders.contains("Content-Type"));
        
        // Verify exposed headers
        assertNotNull(configuration.getExposedHeaders());
        Collection<String> exposedHeaders = Objects.requireNonNull(configuration.getExposedHeaders());
        assertTrue(exposedHeaders.contains("Authorization"));
        assertTrue(exposedHeaders.contains("Content-Type"));
        assertTrue(exposedHeaders.contains("X-Total-Count"));
        
        // Verify that credentials are allowed
        Boolean allowCredentials = configuration.getAllowCredentials();
        assertNotNull(allowCredentials);
        assertTrue(allowCredentials);
        
        // Verify cache time
        assertEquals(3600L, configuration.getMaxAge());
    }
}

