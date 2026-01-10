package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Customer_FVO - Customer For Value Object
 * Used for creating a new Customer according to TMF629 specification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {
    
    @JsonProperty("@type")
    private String type = "Customer";

    @NotNull(message = "DocumentType is required")
    private DocumentType documentType;
    
    @NotBlank(message = "DocumentNumber is required")
    private String documentNumber;
    
    @NotBlank(message = "firstName is required")
    private String firstName;
    
    @NotBlank(message = "lastName is required")
    private String lastName;
    
    private String description;
    
    private String role;
    
    @NotNull(message = "engagedParty is required")
    @Valid
    private PartyRef engagedParty;
    
    private List<@Valid ContactMedium> contactMedium;
    
    private String status;
    
    private String statusReason;
    
    private TimePeriod validFor;
}
