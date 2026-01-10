package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerResponseTest {
    
    @Test
    void testNoArgsConstructor() {
        CustomerResponse response = new CustomerResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getName());
        assertEquals("Customer", response.getType());
        assertEquals("PartyRole", response.getBaseType());
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Juan Perez");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("juan@example.com");
        
        CustomerResponse response = new CustomerResponse(
            "Customer", "PartyRole", "/schema/Customer", 
            "1", "/api/customer/1", DocumentType.CC, "1234567890", "Juan Perez", "Description", "Role",
            engagedParty, Arrays.asList(contactMedium), "Active", "Reason", null,
            now, now
        );
        
        assertEquals("Customer", response.getType());
        assertEquals("PartyRole", response.getBaseType());
        assertEquals("1", response.getId());
        assertEquals("Juan Perez", response.getName());
        assertNotNull(response.getEngagedParty());
        assertNotNull(response.getContactMedium());
        assertEquals(1, response.getContactMedium().size());
        assertEquals("Active", response.getStatus());
        assertEquals(now, response.getCreationDate());
        assertEquals(now, response.getUpdateDate());
    }
    
    @Test
    void testSettersAndGetters() {
        CustomerResponse response = new CustomerResponse();
        LocalDateTime now = LocalDateTime.now();
        
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Maria Garcia");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("maria@example.com");
        contactMedium.setPhoneNumber("9876543210");
        
        response.setId("2");
        response.setHref("/api/customer/2");
        response.setName("Maria Garcia");
        response.setDescription("Test customer");
        response.setRole("Customer");
        response.setEngagedParty(engagedParty);
        response.setContactMedium(Arrays.asList(contactMedium));
        response.setStatus("Active");
        response.setStatusReason("New customer");
        response.setCreationDate(now);
        response.setUpdateDate(now);
        
        assertEquals("2", response.getId());
        assertEquals("/api/customer/2", response.getHref());
        assertEquals("Maria Garcia", response.getName());
        assertEquals("Test customer", response.getDescription());
        assertEquals("Customer", response.getRole());
        assertNotNull(response.getEngagedParty());
        assertEquals("Maria Garcia", response.getEngagedParty().getName());
        assertNotNull(response.getContactMedium());
        assertEquals("maria@example.com", response.getContactMedium().get(0).getEmailAddress());
        assertEquals("Active", response.getStatus());
        assertEquals("New customer", response.getStatusReason());
        assertEquals(now, response.getCreationDate());
        assertEquals(now, response.getUpdateDate());
    }
    
    @Test
    void testDefaultTypeAndBaseType() {
        CustomerResponse response = new CustomerResponse();
        assertEquals("Customer", response.getType());
        assertEquals("PartyRole", response.getBaseType());
    }
    
    @Test
    void testContactMediumList() {
        CustomerResponse response = new CustomerResponse();
        
        ContactMedium emailContact = new ContactMedium();
        emailContact.setType("EmailContactMedium");
        emailContact.setEmailAddress("test@example.com");
        
        ContactMedium phoneContact = new ContactMedium();
        phoneContact.setType("TelephoneContactMedium");
        phoneContact.setPhoneNumber("1234567890");
        
        response.setContactMedium(Arrays.asList(emailContact, phoneContact));
        
        assertNotNull(response.getContactMedium());
        assertEquals(2, response.getContactMedium().size());
        assertEquals("test@example.com", response.getContactMedium().get(0).getEmailAddress());
        assertEquals("1234567890", response.getContactMedium().get(1).getPhoneNumber());
    }
    
    @Test
    void testEngagedParty() {
        CustomerResponse response = new CustomerResponse();
        
        PartyRef party = new PartyRef();
        party.setId("party-1");
        party.setName("Test Party");
        party.setHref("/api/party/party-1");
        party.setReferredType("Individual");
        
        response.setEngagedParty(party);
        
        assertNotNull(response.getEngagedParty());
        assertEquals("party-1", response.getEngagedParty().getId());
        assertEquals("Test Party", response.getEngagedParty().getName());
        assertEquals("Individual", response.getEngagedParty().getReferredType());
    }
    
    @Test
    void testTimePeriod() {
        CustomerResponse response = new CustomerResponse();
        
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusYears(1);
        
        TimePeriod validFor = new TimePeriod();
        validFor.setStartDateTime(start);
        validFor.setEndDateTime(end);
        
        response.setValidFor(validFor);
        
        assertNotNull(response.getValidFor());
        assertEquals(start, response.getValidFor().getStartDateTime());
        assertEquals(end, response.getValidFor().getEndDateTime());
    }
}
