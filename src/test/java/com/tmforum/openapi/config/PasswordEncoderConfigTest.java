package com.tmforum.openapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PasswordEncoderConfigTest {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    void testPasswordEncoderBeanExists() {
        assertNotNull(passwordEncoder);
    }
    
    @Test
    void testPasswordEncoderIsBCrypt() {
        assertTrue(passwordEncoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder);
    }
    
    @Test
    void testPasswordEncoderEncode() {
        String rawPassword = "test-password";
        String encoded = passwordEncoder.encode(rawPassword);
        
        assertNotNull(encoded);
        assertNotEquals(rawPassword, encoded);
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$"));
    }
    
    @Test
    void testPasswordEncoderMatches() {
        String rawPassword = "test-password";
        String encoded = passwordEncoder.encode(rawPassword);
        
        assertTrue(passwordEncoder.matches(rawPassword, encoded));
        assertFalse(passwordEncoder.matches("wrong-password", encoded));
    }
}

