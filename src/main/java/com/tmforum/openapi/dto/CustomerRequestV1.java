package com.tmforum.openapi.dto;

import com.tmforum.openapi.model.DocumentType;
import com.tmforum.openapi.validation.ValidPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestV1 {
    
    @NotNull(message = "DocumentType is required")
    private DocumentType documentType;
    
    @NotBlank(message = "DocumentNumber is required")
    private String documentNumber;
    
    @NotBlank(message = "FirstName is required")
    private String firstName;
    
    @NotBlank(message = "LastName is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must have a valid format")
    // Note: Unique email validation is handled in the service to allow updates
    private String email;
    
    @ValidPhoneNumber
    private String phoneNumber;
    
    @ValidPhoneNumber
    private String mobileNumber;
    
    private String address;
}