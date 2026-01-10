package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Customer_MVO - Customer Modified Value Object
 * Used for partial updates (PATCH) according to TMF629 specification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPatchRequest {
    
    @JsonProperty("@type")
    private String type = "Customer";
    
    private DocumentType documentType;
    
    private String documentNumber;
    
    private String firstName;
    
    private String lastName;
    
    private String description;
    
    private String role;
    
    @Valid
    private PartyRef engagedParty;
    
    private List<@Valid ContactMedium> contactMedium;
    
    private String status;
    
    private String statusReason;
    
    private TimePeriod validFor;
}
