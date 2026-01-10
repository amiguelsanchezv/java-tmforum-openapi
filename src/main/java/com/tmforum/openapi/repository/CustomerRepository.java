package com.tmforum.openapi.repository;

import com.tmforum.openapi.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.tmforum.openapi.model.DocumentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {
    
    // Find customer by email
    Optional<Customer> findByEmail(String email);
    
    // Check if a customer exists with that email
    boolean existsByEmail(String email);
    
    // Find customer by document type and document number
    Optional<Customer> findByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
    
    // Check if a customer exists with that document type and document number
    boolean existsByDocumentTypeAndDocumentNumber(DocumentType documentType, String documentNumber);
    
    // Find customers by name or last name (without pagination)
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    // Find customers by name or last name (with pagination)
    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);
}

