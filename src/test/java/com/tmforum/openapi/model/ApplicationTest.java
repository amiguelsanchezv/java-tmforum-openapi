package com.tmforum.openapi.model;

import org.junit.jupiter.api.Test;
import com.tmforum.openapi.config.SecurityConstants;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    
    @Test
    void testNoArgsConstructor() {
        Application application = new Application();
        assertNotNull(application);
        assertNull(application.getId());
        assertNull(application.getClientId());
        assertNull(application.getClientSecret());
        assertNull(application.getName());
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        Application application = new Application(
            "1", "app-admin", "hashed-secret", "App Admin",
            "Application with administrator permissions", scopes, true,
            now, now
        );
        
        assertEquals("1", application.getId());
        assertEquals("app-admin", application.getClientId());
        assertEquals("hashed-secret", application.getClientSecret());
        assertEquals("App Admin", application.getName());
        assertEquals("Application with administrator permissions", application.getDescription());
        assertEquals(scopes, application.getScopes());
        assertTrue(application.getStatus());
        assertEquals(now, application.getCreationDate());
        assertEquals(now, application.getUpdateDate());
    }
    
    @Test
    void testSettersAndGetters() {
        Application application = new Application();
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        
        application.setId("2");
        application.setClientId("app-readonly");
        application.setClientSecret("new-hashed-secret");
        application.setName("App ReadOnly");
        application.setDescription("Read-only application");
        application.setScopes(scopes);
        application.setStatus(false);
        application.setCreationDate(now);
        application.setUpdateDate(now);
        
        assertEquals("2", application.getId());
        assertEquals("app-readonly", application.getClientId());
        assertEquals("new-hashed-secret", application.getClientSecret());
        assertEquals("App ReadOnly", application.getName());
        assertEquals("Read-only application", application.getDescription());
        assertEquals(scopes, application.getScopes());
        assertFalse(application.getStatus());
        assertEquals(now, application.getCreationDate());
        assertEquals(now, application.getUpdateDate());
    }
    
    @Test
    void testEquals() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes1 = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        List<String> scopes2 = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        
        Application application1 = new Application(
            "1", "app-admin", "secret1", "App Admin",
            "Description", scopes1, true, now, now
        );
        Application application2 = new Application(
            "1", "app-admin", "secret1", "App Admin",
            "Description", scopes2, true, now, now
        );
        Application application3 = new Application(
            "2", "app-readonly", "secret2", "App ReadOnly",
            "Other description", Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ), false, now, now
        );
        
        // Equals
        assertEquals(application1, application2);
        assertEquals(application1, application1); // Reflexive
        
        // Different
        assertNotEquals(application1, application3);
        
        // Null and different types
        assertNotEquals(application1, null);
        assertNotEquals(null, application1);
        assertNotEquals(application1, "not an Application");
        assertNotEquals(application1, new Object());
    }
    
    @Test
    void testEquals_WithDifferentFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        Application base = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        
        // Test all different fields in a single test
        assertNotEquals(base, new Application("2", "app-admin", "secret", "App Admin", "Description", scopes, true, now, now));
        assertNotEquals(base, new Application("1", "app-readonly", "secret", "App Admin", "Description", scopes, true, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret2", "App Admin", "Description", scopes, true, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App ReadOnly", "Description", scopes, true, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App Admin", "Description 2", scopes, true, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App Admin", "Description", Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_WRITE), true, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App Admin", "Description", scopes, false, now, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App Admin", "Description", scopes, true, later, now));
        assertNotEquals(base, new Application("1", "app-admin", "secret", "App Admin", "Description", scopes, true, now, later));
    }
    
    @Test
    void testEquals_WithNullFields() {
        Application application1 = new Application();
        Application application2 = new Application();
        
        assertEquals(application1, application2);
        assertEquals(application1, application1);
    }
    
    @Test
    void testEquals_WithNullId() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        Application application1 = new Application(
            null, "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        Application application2 = new Application(
            null, "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        Application application3 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        
        assertEquals(application1, application2);
        assertNotEquals(application1, application3);
    }
    
    @Test
    void testEquals_WithNullScopes() {
        LocalDateTime now = LocalDateTime.now();
        Application application1 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", null, true, now, now
        );
        Application application2 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", null, true, now, now
        );
        Application application3 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ), true, now, now
        );
        
        assertEquals(application1, application2);
        assertNotEquals(application1, application3);
    }
    
    @Test
    void testEquals_Properties() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        Application application1 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        Application application2 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        Application application3 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        
        // Symmetric: a.equals(b) == b.equals(a)
        assertEquals(application1.equals(application2), application2.equals(application1));
        assertTrue(application1.equals(application2) && application2.equals(application1));
        
        // Transitive: if a.equals(b) and b.equals(c), then a.equals(c)
        assertTrue(application1.equals(application2));
        assertTrue(application2.equals(application3));
        assertTrue(application1.equals(application3));
    }
    
    @Test
    void testHashCode() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes1 = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        List<String> scopes2 = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        List<String> scopes3 = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        
        Application application1 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes1, true, now, now
        );
        Application application2 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes2, true, now, now
        );
        Application application3 = new Application(
            null, "app-admin", "secret", "App Admin",
            "Description", scopes3, true, now, now
        );
        Application application4 = new Application(
            null, "app-admin", "secret", "App Admin",
            "Description", scopes3, true, now, now
        );
        Application application5 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", null, true, now, now
        );
        Application application6 = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", null, true, now, now
        );
        
        // Equal objects must have the same hashCode
        assertEquals(application1.hashCode(), application2.hashCode());
        assertEquals(application3.hashCode(), application4.hashCode());
        assertEquals(application5.hashCode(), application6.hashCode());
        
        // Consistency: same object must have the same hashCode
        assertEquals(application1.hashCode(), application1.hashCode());
        
        // equals/hashCode contract: if they are equal, hashCode must be equal
        assertTrue(application1.equals(application2));
        assertEquals(application1.hashCode(), application2.hashCode());
        
        // With null fields
        Application application7 = new Application();
        Application application8 = new Application();
        assertEquals(application7.hashCode(), application8.hashCode());
        
        // Different values - we only verify that it does not throw an exception
        Application application9 = new Application("2", "app-readonly", "secret2", "App ReadOnly", "Other description", Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_WRITE), false, now, now);
        assertDoesNotThrow(() -> {
            application1.hashCode();
            application9.hashCode();
        });
    }
    
    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        Application application = new Application(
            "1", "app-admin", "secret", "App Admin",
            "Description", scopes, true, now, now
        );
        
        String toString = application.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("app-admin"));
        assertTrue(toString.contains("App Admin"));
    }
    
    @Test
    void testNullFields() {
        Application application = new Application();
        application.setId(null);
        application.setClientId(null);
        application.setClientSecret(null);
        application.setName(null);
        application.setDescription(null);
        application.setScopes(null);
        application.setStatus(null);
        application.setCreationDate(null);
        application.setUpdateDate(null);
        
        assertNull(application.getId());
        assertNull(application.getClientId());
        assertNull(application.getClientSecret());
        assertNull(application.getName());
        assertNull(application.getDescription());
        assertNull(application.getScopes());
        assertNull(application.getStatus());
        assertNull(application.getCreationDate());
        assertNull(application.getUpdateDate());
    }
    
    @Test
    void testEmptyScopes() {
        Application application = new Application();
        application.setScopes(Collections.emptyList());
        
        assertNotNull(application.getScopes());
        assertTrue(application.getScopes().isEmpty());
    }
    
    @Test
    void testStatusTrue() {
        Application application = new Application();
        application.setStatus(true);
        
        assertTrue(application.getStatus());
    }
    
    @Test
    void testStatusFalse() {
        Application application = new Application();
        application.setStatus(false);
        
        assertFalse(application.getStatus());
    }
    
    @Test
    void testMultipleScopes() {
        Application application = new Application();
        List<String> scopes = Arrays.asList(
            SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE, SecurityConstants.SCOPE_CUSTOMERS_DELETE, SecurityConstants.SCOPE_CUSTOMERS_ADMIN
        );
        application.setScopes(scopes);
        
        assertEquals(4, application.getScopes().size());
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
    }
}

