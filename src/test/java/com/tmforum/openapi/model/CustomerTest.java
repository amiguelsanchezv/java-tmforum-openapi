package com.tmforum.openapi.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {
    
    @Test
    void testNoArgsConstructor() {
        Customer customer = new Customer();
        assertNotNull(customer);
        assertNull(customer.getId());
        assertNull(customer.getFirstName());
        assertNull(customer.getLastName());
        assertNull(customer.getEmail());
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        assertEquals("1", customer.getId());
        assertEquals("Juan", customer.getFirstName());
        assertEquals("Perez", customer.getLastName());
        assertEquals("juan@example.com", customer.getEmail());
        assertEquals(DocumentType.CC, customer.getDocumentType());
        assertEquals("1234567890", customer.getDocumentNumber());
        assertEquals("1234567890", customer.getPhoneNumber());
        assertEquals("0987654321", customer.getMobileNumber());
        assertEquals("Street 123", customer.getAddress());
        assertEquals(now, customer.getCreationDate());
        assertEquals(now, customer.getUpdateDate());
    }
    
    @Test
    void testSettersAndGetters() {
        Customer customer = new Customer();
        LocalDateTime now = LocalDateTime.now();
        
        customer.setId("2");
        customer.setFirstName("Maria");
        customer.setLastName("Garcia");
        customer.setEmail("maria@example.com");
        customer.setDocumentType(DocumentType.NIT);
        customer.setDocumentNumber("987654321");
        customer.setPhoneNumber("9876543210");
        customer.setMobileNumber("8765432109");
        customer.setAddress("Avenue 456");
        customer.setCreationDate(now);
        customer.setUpdateDate(now);
        
        assertEquals("2", customer.getId());
        assertEquals("Maria", customer.getFirstName());
        assertEquals("Garcia", customer.getLastName());
        assertEquals("maria@example.com", customer.getEmail());
        assertEquals(DocumentType.NIT, customer.getDocumentType());
        assertEquals("987654321", customer.getDocumentNumber());
        assertEquals("9876543210", customer.getPhoneNumber());
        assertEquals("8765432109", customer.getMobileNumber());
        assertEquals("Avenue 456", customer.getAddress());
        assertEquals(now, customer.getCreationDate());
        assertEquals(now, customer.getUpdateDate());
    }
    
    @Test
    void testEquals() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer1 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer2 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer3 = new Customer(
            "2", "Maria", "Garcia", "maria@example.com",
            DocumentType.NIT, "987654321",
            "9876543210", "8765432109", "Avenue 456",
            now, now
        );
        
        // Equals
        assertEquals(customer1, customer2);
        assertEquals(customer1, customer1); // Reflexive
        
        // Different fields
        assertNotEquals(customer1, customer3);
        
        // Null and different types
        assertNotEquals(customer1, null);
        assertNotEquals(null, customer1);
        assertNotEquals(customer1, "not a Customer");
        assertNotEquals(customer1, new Object());
    }
    
    @Test
    void testEquals_WithDifferentFields() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime later = now.plusHours(1);
        Customer base = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        // Test all different fields in a single test
        assertNotEquals(base, new Customer("2", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Maria", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Garcia", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "maria@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.TI, "1234567890", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "987654321", "1234567890", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "9876543210", "0987654321", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "8765432109", "Street 123", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Avenue 456", now, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", later, now));
        assertNotEquals(base, new Customer("1", "Juan", "Perez", "juan@example.com", DocumentType.CC, "1234567890", "1234567890", "0987654321", "Street 123", now, later));
    }
    
    @Test
    void testEquals_WithNullFields() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();
        
        assertEquals(customer1, customer2);
        assertEquals(customer1, customer1);
    }
    
    @Test
    void testEquals_WithNullId() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer1 = new Customer(
            null, "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer2 = new Customer(
            null, "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer3 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        assertEquals(customer1, customer2);
        assertNotEquals(customer1, customer3);
    }
    
    @Test
    void testEquals_Properties() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer1 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer2 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer3 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        // Symmetric: a.equals(b) == b.equals(a)
        assertEquals(customer1.equals(customer2), customer2.equals(customer1));
        assertTrue(customer1.equals(customer2) && customer2.equals(customer1));
        
        // Transitive: if a.equals(b) and b.equals(c), then a.equals(c)
        assertTrue(customer1.equals(customer2));
        assertTrue(customer2.equals(customer3));
        assertTrue(customer1.equals(customer3));
    }
    
    @Test
    void testHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer1 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer2 = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer3 = new Customer(
            null, "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        Customer customer4 = new Customer(
            null, "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        // Equal objects must have the same hashCode
        assertEquals(customer1.hashCode(), customer2.hashCode());
        assertEquals(customer3.hashCode(), customer4.hashCode());
        
        // Consistency: same object must have the same hashCode
        assertEquals(customer1.hashCode(), customer1.hashCode());
        
        // equals/hashCode contract: if they are equal, hashCode must be equal
        assertTrue(customer1.equals(customer2));
        assertEquals(customer1.hashCode(), customer2.hashCode());
        
        // With null fields
        Customer customer5 = new Customer();
        Customer customer6 = new Customer();
        assertEquals(customer5.hashCode(), customer6.hashCode());
        
        // Different values - we only verify that it does not throw an exception
        Customer customer7 = new Customer("2", "Maria", "Garcia", "maria@example.com", DocumentType.NIT, "987654321", "9876543210", "8765432109", "Avenue 456", now, now);
        assertDoesNotThrow(() -> {
            customer1.hashCode();
            customer7.hashCode();
        });
    }
    
    @Test
    void testToString() {
        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer(
            "1", "Juan", "Perez", "juan@example.com",
            DocumentType.CC, "1234567890",
            "1234567890", "0987654321", "Street 123",
            now, now
        );
        
        String toString = customer.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("1"));
        assertTrue(toString.contains("Juan"));
        assertTrue(toString.contains("Perez"));
        assertTrue(toString.contains("juan@example.com"));
    }
    
    @Test
    void testNullFields() {
        Customer customer = new Customer();
        customer.setId(null);
        customer.setFirstName(null);
        customer.setLastName(null);
        customer.setEmail(null);
        customer.setDocumentType(null);
        customer.setDocumentNumber(null);
        customer.setPhoneNumber(null);
        customer.setMobileNumber(null);
        customer.setAddress(null);
        customer.setCreationDate(null);
        customer.setUpdateDate(null);
        
        assertNull(customer.getId());
        assertNull(customer.getFirstName());
        assertNull(customer.getLastName());
        assertNull(customer.getEmail());
        assertNull(customer.getDocumentType());
        assertNull(customer.getDocumentNumber());
        assertNull(customer.getPhoneNumber());
        assertNull(customer.getMobileNumber());
        assertNull(customer.getAddress());
        assertNull(customer.getCreationDate());
        assertNull(customer.getUpdateDate());
    }
    
    @Test
    void testPartialFields() {
        Customer customer = new Customer();
        customer.setFirstName("Carlos");
        customer.setEmail("carlos@example.com");
        // Other fields remain null
        
        assertEquals("Carlos", customer.getFirstName());
        assertEquals("carlos@example.com", customer.getEmail());
        assertNull(customer.getId());
        assertNull(customer.getLastName());
    }
}

