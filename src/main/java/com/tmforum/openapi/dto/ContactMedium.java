package com.tmforum.openapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactMedium {
    
    @JsonProperty("@type")
    private String type;
    
    private String preferred;
    
    private String emailAddress;
    
    private String phoneNumber;
    
    private String mobileNumber;
    
    private String faxNumber;
    
    private String city;
    
    private String country;
    
    private String postCode;
    
    private String stateOrProvince;
    
    private String street1;
    
    private String street2;
}
