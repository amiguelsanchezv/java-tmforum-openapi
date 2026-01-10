package com.tmforum.openapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiConfigTest {
    
    @Autowired
    private OpenAPI openAPI;
    
    @Test
    void testOpenAPIBeanExists() {
        assertNotNull(openAPI);
    }
    
    @Test
    void testOpenAPIInfo() {
        assertNotNull(openAPI.getInfo());
        assertEquals("TMForum Open API REST API", openAPI.getInfo().getTitle());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
        assertNotNull(openAPI.getInfo().getDescription());
        assertTrue(openAPI.getInfo().getDescription().contains("Spring Boot"));
        assertTrue(openAPI.getInfo().getDescription().contains("MongoDB"));
    }
    
    @Test
    void testOpenAPIContact() {
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("Development Team", openAPI.getInfo().getContact().getName());
        assertEquals("development@gmail.com", openAPI.getInfo().getContact().getEmail());
    }
    
    @Test
    void testOpenAPILicense() {
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("MIT License", openAPI.getInfo().getLicense().getName());
        assertNotNull(openAPI.getInfo().getLicense().getUrl());
    }
    
    @Test
    void testOpenAPISecurityScheme() {
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertNotNull(openAPI.getComponents().getSecuritySchemes().get("bearerAuth"));
        
        var securityScheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertEquals("bearerAuth", securityScheme.getName());
        assertEquals(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
    }
    
    @Test
    void testOpenAPISecurityRequirement() {
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().stream()
            .anyMatch(req -> req.containsKey("bearerAuth")));
    }
}

