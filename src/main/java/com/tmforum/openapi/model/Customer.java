package com.tmforum.openapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "customers")
@CompoundIndex(name = "documentType_documentNumber_idx", def = "{'documentType': 1, 'documentNumber': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    private String id;
    
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private DocumentType documentType;
    
    private String documentNumber;
    
    private String phoneNumber;
    
    private String mobileNumber;
    
    private String address;
    
    @CreatedDate
    private LocalDateTime creationDate;
    
    @LastModifiedDate
    private LocalDateTime updateDate;
}

