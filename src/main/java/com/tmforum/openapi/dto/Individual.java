package com.tmforum.openapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Individual {
    
    @JsonProperty("@type")
    private String type = "Individual";
    
    private String id;
    
    private String href;
    
    private String givenName;
    
    private String familyName;
    
    private String middleName;
    
    private String fullName;
    
    private String emailAddress;
    
    private String phoneNumber;
    
    private String mobileNumber;
    
    private LocalDateTime birthDate;
    
    private String gender;
    
    private String nationality;
    
    private String maritalStatus;
}
