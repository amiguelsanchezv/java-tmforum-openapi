package com.tmforum.openapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    // Audit configuration is enabled with @EnableMongoAuditing
    // Dates are automatically generated with @CreatedDate and @LastModifiedDate
    // creationDate is automatically set when creating
    // updateDate is automatically updated when modifying
    
    @Autowired
    private MappingMongoConverter mongoConverter;
    
    @PostConstruct
    public void removeClassField() {
        mongoConverter.setTypeMapper(new DefaultMongoTypeMapper(null));
    }
}

