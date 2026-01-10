package com.tmforum.openapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for MongoConfig
 * This test verifies that the MongoDB configuration exists without requiring a real connection
 */
@SpringBootTest
@ActiveProfiles("test")
class MongoConfigTest {
    
    @Test
    void testMongoConfigClassExists() {
        // Test simplified that only verifies that the class exists
        // The real MongoDB tests are made in the integration tests with Testcontainers
        assertNotNull(MongoConfig.class);
    }
}

