package com.tmforum.openapi.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {
    
    @Test
    void testNoArgsConstructor() {
        LoginRequest request = new LoginRequest();
        assertNotNull(request);
        assertNull(request.getClientId());
        assertNull(request.getClientSecret());
    }
    
    @Test
    void testAllArgsConstructor() {
        LoginRequest request = new LoginRequest("app-admin", "secret-admin");
        
        assertEquals("app-admin", request.getClientId());
        assertEquals("secret-admin", request.getClientSecret());
    }
    
    @Test
    void testSettersAndGetters() {
        LoginRequest request = new LoginRequest();
        
        request.setClientId("app-readonly");
        request.setClientSecret("secret-readonly");
        
        assertEquals("app-readonly", request.getClientId());
        assertEquals("secret-readonly", request.getClientSecret());
    }
    
    @Test
    void testEquals() {
        LoginRequest request1 = new LoginRequest("app-admin", "secret-admin");
        LoginRequest request2 = new LoginRequest("app-admin", "secret-admin");
        LoginRequest request3 = new LoginRequest("app-readonly", "secret-readonly");
        LoginRequest request4 = new LoginRequest(null, null);
        LoginRequest request5 = new LoginRequest(null, null);
        LoginRequest request6 = new LoginRequest("app-admin", null);
        
        // Equals
        assertEquals(request1, request2);
        assertEquals(request1, request1); // Reflexive
        assertEquals(request4, request5);
        
        // Different
        assertNotEquals(request1, request3);
        assertNotEquals(request1, new LoginRequest("app-readonly", "secret-admin"));
        assertNotEquals(request1, new LoginRequest("app-admin", "secret-readonly"));
        assertNotEquals(request4, request6);
        
        // Null and different types
        assertNotEquals(request1, null);
        assertNotEquals(request1, "not a LoginRequest");
    }
    
    @Test
    void testHashCode() {
        LoginRequest request1 = new LoginRequest("app-admin", "secret-admin");
        LoginRequest request2 = new LoginRequest("app-admin", "secret-admin");
        LoginRequest request3 = new LoginRequest(null, null);
        LoginRequest request4 = new LoginRequest(null, null);
        
        // Equal objects must have the same hashCode
        assertEquals(request1.hashCode(), request2.hashCode());
        assertEquals(request3.hashCode(), request4.hashCode());
        
        // Consistency
        assertEquals(request1.hashCode(), request1.hashCode());
        
        // equals/hashCode contract
        assertTrue(request1.equals(request2));
        assertEquals(request1.hashCode(), request2.hashCode());
    }
    
    @Test
    void testToString() {
        LoginRequest request = new LoginRequest("app-admin", "secret-admin");
        
        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("app-admin"));
        assertTrue(toString.contains("secret-admin"));
    }
}

