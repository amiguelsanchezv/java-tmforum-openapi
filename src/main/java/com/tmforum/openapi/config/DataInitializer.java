package com.tmforum.openapi.config;

import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.tmforum.openapi.config.SecurityConstants;

import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Profile("!test") // Do not run in test profile
@ConditionalOnProperty(name = "data.initializer.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.readonly.secret:secret-readonly}")
    private String readonlySecret;
    
    @Value("${app.readwrite.secret:secret-readwrite}")
    private String readwriteSecret;
    
    @Value("${app.admin.secret:secret-admin}")
    private String adminSecret;
    
    @Override
    public void run(String... args) throws Exception {
        // Clean up duplicates before creating applications
        cleanupDuplicates();
        // Create applications if they don't exist
        createApplications();
    }
    
    private void cleanupDuplicates() {
        // Clean up any duplicate applications for the standard clientIds
        // This ensures we only have one active application per clientId
        String[] clientIds = {"app-readonly", "app-readwrite", "app-admin"};
        var allApplications = applicationRepository.findAll();
        
        for (String clientId : clientIds) {
            var applications = allApplications.stream()
                    .filter(app -> clientId.equals(app.getClientId()))
                    .toList();
            
            if (applications.size() > 1) {
                // Keep the first one, delete the rest
                for (int i = 1; i < applications.size(); i++) {
                    applicationRepository.delete(applications.get(i));
                }
            }
        }
    }
    
    private void createApplications() {
        // Application with read-only access
        if (!applicationRepository.existsByClientId("app-readonly")) {
            Application appReadOnly = new Application();
            appReadOnly.setClientId("app-readonly");
            appReadOnly.setClientSecret(passwordEncoder.encode(readonlySecret));
            appReadOnly.setName("Read-Only Application");
            appReadOnly.setDescription("Application with read-only permissions");
            appReadOnly.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
            appReadOnly.setStatus(true);
            appReadOnly.setCreationDate(LocalDateTime.now());
            appReadOnly.setUpdateDate(LocalDateTime.now());
            applicationRepository.save(appReadOnly);
        }
        
        // Application with read and write access
        if (!applicationRepository.existsByClientId("app-readwrite")) {
            Application appReadWrite = new Application();
            appReadWrite.setClientId("app-readwrite");
            appReadWrite.setClientSecret(passwordEncoder.encode(readwriteSecret));
            appReadWrite.setName("Read/Write Application");
            appReadWrite.setDescription("Application with read and write permissions");
            appReadWrite.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE));
            appReadWrite.setStatus(true);
            appReadWrite.setCreationDate(LocalDateTime.now());
            appReadWrite.setUpdateDate(LocalDateTime.now());
            applicationRepository.save(appReadWrite);
        }
        
        // Application with full access (admin)
        if (!applicationRepository.existsByClientId("app-admin")) {
            Application appAdmin = new Application();
            appAdmin.setClientId("app-admin");
            appAdmin.setClientSecret(passwordEncoder.encode(adminSecret));
            appAdmin.setName("Administrator Application");
            appAdmin.setDescription("Application with all permissions");
            appAdmin.setScopes(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE, SecurityConstants.SCOPE_CUSTOMERS_DELETE, SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
            appAdmin.setStatus(true);
            appAdmin.setCreationDate(LocalDateTime.now());
            appAdmin.setUpdateDate(LocalDateTime.now());
            applicationRepository.save(appAdmin);
        }
    }
}

