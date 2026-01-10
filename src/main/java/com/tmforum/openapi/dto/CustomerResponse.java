package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer - Full Customer object according to TMF629 specification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    
    @JsonProperty("@type")
    private String type = "Customer";
    
    @JsonProperty("@baseType")
    private String baseType = "PartyRole";
    
    @JsonProperty("@schemaLocation")
    private String schemaLocation;
    
    private String id;
    
    private String href;

    private DocumentType documentType;
    
    private String documentNumber;
    
    private String name;
    
    private String description;
    
    private String role;
    
    private PartyRef engagedParty;
    
    private List<ContactMedium> contactMedium;
    
    private String status;
    
    private String statusReason;
    
    private TimePeriod validFor;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateDate;
}
