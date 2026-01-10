package com.tmforum.openapi.repository;

import com.tmforum.openapi.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    
    // Find application by clientId
    Optional<Application> findByClientId(String clientId);
    
    // Check if an application exists with that clientId
    boolean existsByClientId(String clientId);
    
    // Find application by clientId and status true
    Optional<Application> findByClientIdAndStatusTrue(String clientId);
}

