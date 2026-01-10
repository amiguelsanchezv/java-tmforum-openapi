package com.tmforum.openapi.service;

import com.tmforum.openapi.dto.LoginRequest;
import com.tmforum.openapi.dto.LoginResponse;
import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import com.tmforum.openapi.util.JwtUtil;
import com.tmforum.openapi.config.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class AuthService {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest) {
        // Find application by clientId
        Application application = applicationRepository.findByClientIdAndStatusTrue(loginRequest.getClientId())
                .orElseThrow(() -> new RuntimeException("ClientId not found or inactive"));
        
        // Validate clientSecret
        if (!passwordEncoder.matches(loginRequest.getClientSecret(), application.getClientSecret())) {
            throw new RuntimeException("Incorrect ClientSecret");
        }
        
        // Get application scopes
        List<String> scopes = application.getScopes();
        if (scopes == null || scopes.isEmpty()) {
            // Default scopes if none assigned
            scopes = Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ);
        }
        
        // Generate JWT token with application scopes
        String[] scopesArray = scopes.toArray(new String[0]);
        String token = jwtUtil.generateToken(loginRequest.getClientId(), scopesArray);
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setClientId(loginRequest.getClientId());
        response.setScopes(scopesArray);
        
        return response;
    }
    
    public Application createApplication(String clientId, String clientSecret, String name, 
                                     String description, List<String> scopes) {
        if (applicationRepository.existsByClientId(clientId)) {
            throw new RuntimeException("An application with that clientId already exists");
        }
        
        Application application = new Application();
        application.setClientId(clientId);
        application.setClientSecret(passwordEncoder.encode(clientSecret));
        application.setName(name);
        application.setDescription(description);
        application.setScopes(scopes);
        application.setStatus(true);
        application.setCreationDate(LocalDateTime.now());
        application.setUpdateDate(LocalDateTime.now());
        
        return applicationRepository.save(application);
    }
}

