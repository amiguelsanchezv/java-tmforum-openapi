package com.tmforum.openapi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String clientId;
    
    private String clientSecret; // Hash BCrypt
    
    private String name;
    
    private String description;
    
    private List<String> scopes; // Scopes list allowed
    
    private Boolean status;
    
    private LocalDateTime creationDate;
    
    private LocalDateTime updateDate;
}

