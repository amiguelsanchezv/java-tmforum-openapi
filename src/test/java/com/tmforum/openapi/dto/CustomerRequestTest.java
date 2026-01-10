package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRequestTest {
    
    @Test
    void testNoArgsConstructor() {
        CustomerRequest request = new CustomerRequest();
        assertNotNull(request);
        assertNull(request.getFirstName());
        assertNull(request.getLastName());
        assertNull(request.getEngagedParty());
        assertNull(request.getContactMedium());
        assertEquals("Customer", request.getType());
    }
    
    @Test
    void testAllArgsConstructor() {
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Juan Perez");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("juan@example.com");
        
        CustomerRequest request = new CustomerRequest(
            "Customer", DocumentType.CC, "1234567890", "Juan", "Perez", "Description", "Role",
            engagedParty, Arrays.asList(contactMedium), "Active", "Reason", null
        );
        
        assertEquals("Customer", request.getType());
        assertEquals("Juan", request.getFirstName());
        assertEquals("Perez", request.getLastName());
        assertEquals("Description", request.getDescription());
        assertEquals("Role", request.getRole());
        assertNotNull(request.getEngagedParty());
        assertNotNull(request.getContactMedium());
        assertEquals(1, request.getContactMedium().size());
        assertEquals("Active", request.getStatus());
    }
    
    @Test
    void testSettersAndGetters() {
        CustomerRequest request = new CustomerRequest();
        
        PartyRef engagedParty = new PartyRef();
        engagedParty.setName("Maria Garcia");
        
        ContactMedium contactMedium = new ContactMedium();
        contactMedium.setEmailAddress("maria@example.com");
        contactMedium.setPhoneNumber("9876543210");
        
        request.setFirstName("Maria");
        request.setLastName("Garcia");
        request.setDescription("Test customer");
        request.setRole("Customer");
        request.setEngagedParty(engagedParty);
        request.setContactMedium(Arrays.asList(contactMedium));
        request.setStatus("Active");
        request.setStatusReason("New customer");
        
        assertEquals("Maria", request.getFirstName());
        assertEquals("Garcia", request.getLastName());
        assertEquals("Test customer", request.getDescription());
        assertEquals("Customer", request.getRole());
        assertNotNull(request.getEngagedParty());
        assertEquals("Maria Garcia", request.getEngagedParty().getName());
        assertNotNull(request.getContactMedium());
        assertEquals("maria@example.com", request.getContactMedium().get(0).getEmailAddress());
        assertEquals("Active", request.getStatus());
        assertEquals("New customer", request.getStatusReason());
    }
    
    @Test
    void testDefaultType() {
        CustomerRequest request = new CustomerRequest();
        assertEquals("Customer", request.getType());
    }
    
    @Test
    void testContactMediumList() {
        CustomerRequest request = new CustomerRequest();
        
        ContactMedium emailContact = new ContactMedium();
        emailContact.setType("EmailContactMedium");
        emailContact.setEmailAddress("test@example.com");
        
        ContactMedium phoneContact = new ContactMedium();
        phoneContact.setType("TelephoneContactMedium");
        phoneContact.setPhoneNumber("1234567890");
        
        request.setContactMedium(Arrays.asList(emailContact, phoneContact));
        
        assertNotNull(request.getContactMedium());
        assertEquals(2, request.getContactMedium().size());
        assertEquals("test@example.com", request.getContactMedium().get(0).getEmailAddress());
        assertEquals("1234567890", request.getContactMedium().get(1).getPhoneNumber());
    }
    
    @Test
    void testEngagedPartyRequired() {
        CustomerRequest request = new CustomerRequest();
        // engagedParty should be set for valid requests
        assertNull(request.getEngagedParty());
        
        PartyRef party = new PartyRef();
        party.setId("party-1");
        party.setName("Test Party");
        request.setEngagedParty(party);
        
        assertNotNull(request.getEngagedParty());
        assertEquals("party-1", request.getEngagedParty().getId());
    }
}
