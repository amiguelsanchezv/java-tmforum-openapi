package com.tmforum.openapi.dto;

import org.junit.jupiter.api.Test;
import com.tmforum.openapi.config.SecurityConstants;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {
    
    @Test
    void testNoArgsConstructor() {
        LoginResponse response = new LoginResponse();
        assertNotNull(response);
        assertNull(response.getToken());
        assertEquals("Bearer", response.getType()); // Default value
        assertNull(response.getClientId());
        assertNull(response.getScopes());
        
        // Verify that the default type is maintained
        assertEquals("Bearer", response.getType());
    }
    
    @Test
    void testAllArgsConstructor() {
        String[] scopes = {SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE};
        LoginResponse response = new LoginResponse(
            "test-token", "Bearer", "app-admin", scopes
        );
        
        assertEquals("test-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals("app-admin", response.getClientId());
        assertArrayEquals(scopes, response.getScopes());
    }
    
    @Test
    void testSettersAndGetters() {
        LoginResponse response = new LoginResponse();
        String[] scopes = {SecurityConstants.SCOPE_CUSTOMERS_READ};
        
        response.setToken("new-token");
        response.setType("JWT");
        response.setClientId("app-readonly");
        response.setScopes(scopes);
        
        assertEquals("new-token", response.getToken());
        assertEquals("JWT", response.getType());
        assertEquals("app-readonly", response.getClientId());
        assertArrayEquals(scopes, response.getScopes());
    }
    
    @Test
    void testEquals() {
        String[] scopes1 = {SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE};
        String[] scopes2 = {SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE};
        String[] scopes3 = {SecurityConstants.SCOPE_CUSTOMERS_READ};
        
        LoginResponse response1 = new LoginResponse(
            "token1", "Bearer", "app-admin", scopes1
        );
        LoginResponse response2 = new LoginResponse(
            "token1", "Bearer", "app-admin", scopes2
        );
        LoginResponse response3 = new LoginResponse(
            "token2", "Bearer", "app-readonly", scopes3
        );
        LoginResponse response4 = new LoginResponse(
            null, null, null, null
        );
        LoginResponse response5 = new LoginResponse(
            null, null, null, null
        );
        LoginResponse response6 = new LoginResponse(
            "token", null, null, null
        );
        
        // Equals
        assertEquals(response1, response2);
        assertEquals(response1, response1); // Reflexive
        assertEquals(response4, response5);
        
        // Different
        assertNotEquals(response1, response3);
        assertNotEquals(response1, new LoginResponse("token2", "Bearer", "app-admin", scopes1));
        assertNotEquals(response1, new LoginResponse("token1", "JWT", "app-admin", scopes1));
        assertNotEquals(response1, new LoginResponse("token1", "Bearer", "app-readonly", scopes1));
        assertNotEquals(response1, new LoginResponse("token1", "Bearer", "app-admin", scopes3));
        assertNotEquals(response4, response6);
        
        // Null and different types.
        assertNotEquals(response1, null);
        assertNotEquals(response1, "not a LoginResponse");
    }
    
    @Test
    void testHashCode() {
        String[] scopes1 = {SecurityConstants.SCOPE_CUSTOMERS_READ};
        String[] scopes2 = {SecurityConstants.SCOPE_CUSTOMERS_READ};
        
        LoginResponse response1 = new LoginResponse(
            "token", "Bearer", "app-admin", scopes1
        );
        LoginResponse response2 = new LoginResponse(
            "token", "Bearer", "app-admin", scopes2
        );
        LoginResponse response3 = new LoginResponse(
            null, null, null, null
        );
        LoginResponse response4 = new LoginResponse(
            null, null, null, null
        );
        
        // Equal objects must have the same hashCode
        assertEquals(response1.hashCode(), response2.hashCode());
        assertEquals(response3.hashCode(), response4.hashCode());
        
        // Consistency
        assertEquals(response1.hashCode(), response1.hashCode());
        
        // equals/hashCode contract
        assertTrue(response1.equals(response2));
        assertEquals(response1.hashCode(), response2.hashCode());
    }
    
    @Test
    void testToString() {
        String[] scopes = {SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE};
        LoginResponse response = new LoginResponse(
            "test-token", "Bearer", "app-admin", scopes
        );
        
        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("test-token"));
        assertTrue(toString.contains("app-admin"));
    }
    
    @Test
    void testEmptyScopes() {
        LoginResponse response = new LoginResponse();
        response.setScopes(new String[0]);
        
        assertNotNull(response.getScopes());
        assertEquals(0, response.getScopes().length);
    }
}

