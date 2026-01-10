package com.tmforum.openapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tmforum.openapi.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseV1 {
    
    private String id;
    private DocumentType documentType;
    private String documentNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String address;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime creationDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updateDate;
}

