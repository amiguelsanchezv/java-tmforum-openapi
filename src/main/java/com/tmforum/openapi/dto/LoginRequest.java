package com.tmforum.openapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "The clientId is required")
    private String clientId;
    
    @NotBlank(message = "The clientSecret is required")
    private String clientSecret;
}

