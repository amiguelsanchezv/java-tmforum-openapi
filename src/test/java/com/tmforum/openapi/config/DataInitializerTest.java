package com.tmforum.openapi.config;

import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.tmforum.openapi.config.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Testcontainers
@EnabledIfEnvironmentVariable(named = "DOCKER_AVAILABLE", matches = "true", disabledReason = "Docker unavailable. Run with -DDOCKER_AVAILABLE=true if Docker is available")
class DataInitializerTest {
    
    @Container
    @SuppressWarnings("resource")
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withReuse(true);
    
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        if (mongoDBContainer.isRunning()) {
            registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
            registry.add("spring.autoconfigure.exclude", () -> "");
        }
    }
    
    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private DataInitializer dataInitializer;
    
    @BeforeEach
    void setUp() {
        // Clean the database before each test
        // This ensures that each test starts with a clean state
        applicationRepository.deleteAll();
    }
    
    @Test
    void testDataInitializerCreatesAppReadOnly() {
        // Invoke manually the method to create the applications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Verify that the application app-readonly was created
        Optional<Application> app = applicationRepository.findByClientIdAndStatusTrue("app-readonly");
        
        assertTrue(app.isPresent(), "The application app-readonly should exist");
        Application application = app.get();
        assertEquals("app-readonly", application.getClientId());
        assertEquals("Read-Only Application", application.getName());
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertEquals(1, application.getScopes().size());
        assertTrue(application.getStatus());
        assertTrue(passwordEncoder.matches("secret-readonly", application.getClientSecret()));
    }
    
    @Test
    void testDataInitializerCreatesAppReadWrite() {
        // Invoke manually the method to create the applications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        Optional<Application> app = applicationRepository.findByClientIdAndStatusTrue("app-readwrite");
        
        assertTrue(app.isPresent(), "The application app-readwrite should exist");
        Application application = app.get();
        assertEquals("app-readwrite", application.getClientId());
        assertEquals("Read/Write Application", application.getName());
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        assertEquals(2, application.getScopes().size());
        assertTrue(application.getStatus());
        assertTrue(passwordEncoder.matches("secret-readwrite", application.getClientSecret()));
    }
    
    @Test
    void testDataInitializerCreatesAppAdmin() {
        // Invoke manually the method to create the applications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        Optional<Application> app = applicationRepository.findByClientIdAndStatusTrue("app-admin");
        
        assertTrue(app.isPresent(), "The application app-admin should exist");
        Application application = app.get();
        assertEquals("app-admin", application.getClientId());
        assertEquals("Administrator Application", application.getName());
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_DELETE));
        assertTrue(application.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
        assertEquals(4, application.getScopes().size());
        assertTrue(application.getStatus());
        assertTrue(passwordEncoder.matches("secret-admin", application.getClientSecret()));
    }
    
    @Test
    void testDataInitializerCreatesAllThreeApps() {
        // Invoke manually the method to create the applications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        long count = applicationRepository.count();
        assertEquals(3, count, "There should be exactly 3 initial applications");
        
        assertTrue(applicationRepository.existsByClientId("app-readonly"));
        assertTrue(applicationRepository.existsByClientId("app-readwrite"));
        assertTrue(applicationRepository.existsByClientId("app-admin"));
    }
    
    @Test
    void testCreateApplications_DirectInvocation() {
        // Verify that there are no applications (already cleaned in setUp)
        assertEquals(0, applicationRepository.count());
        
        // Invoke directly the private createApplications method using reflection
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Verify that the 3 applications were created
        assertEquals(3, applicationRepository.count());
        
        // Verify app-readonly
        Optional<Application> appReadOnly = applicationRepository.findByClientIdAndStatusTrue("app-readonly");
        assertTrue(appReadOnly.isPresent());
        Application app1 = appReadOnly.get();
        assertEquals("app-readonly", app1.getClientId());
        assertEquals("Read-Only Application", app1.getName());
        assertEquals("Application with read-only permissions", app1.getDescription());
        assertEquals(1, app1.getScopes().size());
        assertTrue(app1.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(app1.getStatus());
        assertTrue(passwordEncoder.matches("secret-readonly", app1.getClientSecret()));
        assertNotNull(app1.getCreationDate());
        assertNotNull(app1.getUpdateDate());
        
        // Verify app-readwrite
        Optional<Application> appReadWrite = applicationRepository.findByClientIdAndStatusTrue("app-readwrite");
        assertTrue(appReadWrite.isPresent());
        Application app2 = appReadWrite.get();
        assertEquals("app-readwrite", app2.getClientId());
        assertEquals("Read/Write Application", app2.getName());
        assertEquals("Application with read and write permissions", app2.getDescription());
        assertEquals(2, app2.getScopes().size());
        assertTrue(app2.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(app2.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        assertTrue(app2.getStatus());
        assertTrue(passwordEncoder.matches("secret-readwrite", app2.getClientSecret()));
        assertNotNull(app2.getCreationDate());
        assertNotNull(app2.getUpdateDate());
        
        // Verify app-admin
        Optional<Application> appAdmin = applicationRepository.findByClientIdAndStatusTrue("app-admin");
        assertTrue(appAdmin.isPresent());
        Application app3 = appAdmin.get();
        assertEquals("app-admin", app3.getClientId());
        assertEquals("Administrator Application", app3.getName());
        assertEquals("Application with all permissions", app3.getDescription());
        assertEquals(4, app3.getScopes().size());
        assertTrue(app3.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(app3.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        assertTrue(app3.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_DELETE));
        assertTrue(app3.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
        assertTrue(app3.getStatus());
        assertTrue(passwordEncoder.matches("secret-admin", app3.getClientSecret()));
        assertNotNull(app3.getCreationDate());
        assertNotNull(app3.getUpdateDate());
    }
    
    @Test
    void testCreateApplications_DoesNotCreateDuplicates() {
        // First create the initial applications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Verify that the 3 applications were created
        long countBefore = applicationRepository.count();
        assertEquals(3, countBefore, "There should be exactly 3 initial applications");
        
        // Save the IDs of the existing applications
        var appsBefore = applicationRepository.findAll();
        var idsBefore = appsBefore.stream()
            .map(app -> app.getId())
            .collect(java.util.stream.Collectors.toSet());
        
        // Invoke the method again - it should not create duplicates
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Verify that the count did not change
        long countAfter = applicationRepository.count();
        assertEquals(countBefore, countAfter, "It should not create duplicates");
        
        // Verify that the IDs are the same (no new applications were created)
        var appsAfter = applicationRepository.findAll();
        var idsAfter = appsAfter.stream()
            .map(app -> app.getId())
            .collect(java.util.stream.Collectors.toSet());
        assertEquals(idsBefore, idsAfter, "The IDs should be the same, no new applications were created");
        
        // Verify that there is only one instance of each application
        assertEquals(1, applicationRepository.findAll().stream()
            .filter(app -> "app-readonly".equals(app.getClientId()))
            .count(), "There should be only one instance of app-readonly");
        assertEquals(1, applicationRepository.findAll().stream()
            .filter(app -> "app-readwrite".equals(app.getClientId()))
            .count(), "There should be only one instance of app-readwrite");
        assertEquals(1, applicationRepository.findAll().stream()
            .filter(app -> "app-admin".equals(app.getClientId()))
            .count(), "There should be only one instance of app-admin");
    }
    
    @Test
    void testCreateApplications_WhenOneAppExists() {
        // Create app-readonly manually
        Application existingApp = new Application();
        existingApp.setClientId("app-readonly");
        existingApp.setClientSecret(passwordEncoder.encode("existing-secret"));
        existingApp.setName("Existing App");
        existingApp.setScopes(java.util.Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        existingApp.setStatus(true);
        applicationRepository.save(existingApp);
        
        assertEquals(1, applicationRepository.count(), "There should be only 1 application before invoking createApplications");
        
        // Invoke createApplications - it should create only the missing ones
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // There should be 3 applications (1 existing + 2 new)
        long count = applicationRepository.count();
        assertEquals(3, count, "There should be exactly 3 applications (1 existing + 2 new)");
        
        // Verify that app-readonly is still the original (not overwritten)
        Optional<Application> app = applicationRepository.findByClientIdAndStatusTrue("app-readonly");
        assertTrue(app.isPresent());
        assertEquals("Existing App", app.get().getName(), "It should not overwrite the existing application");
        
        // Verify that the other two were created
        assertTrue(applicationRepository.existsByClientId("app-readwrite"), "app-readwrite should exist");
        assertTrue(applicationRepository.existsByClientId("app-admin"), "app-admin should exist");
    }
    
    @Test
    void testRun_WhenNoApplications_CreatesInitialApplications() throws Exception {
        // Given - no applications (already cleaned in setUp)
        assertEquals(0, applicationRepository.count());
        
        // When - invoke the run() method directly
        dataInitializer.run();
        
        // Then - it should create the 3 initial applications
        assertEquals(3, applicationRepository.count());
        assertTrue(applicationRepository.existsByClientId("app-readonly"));
        assertTrue(applicationRepository.existsByClientId("app-readwrite"));
        assertTrue(applicationRepository.existsByClientId("app-admin"));
    }
    
    @Test
    void testRun_WhenApplicationsExist_DoesNotCreate() throws Exception {
        // Given - create an application manually
        Application existingApp = new Application();
        existingApp.setClientId("existing-app");
        existingApp.setClientSecret(passwordEncoder.encode("secret"));
        existingApp.setName("Existing Application");
        existingApp.setScopes(java.util.Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        existingApp.setStatus(true);
        applicationRepository.save(existingApp);
        
        assertEquals(1, applicationRepository.count());
        
        // When - invoke the run() method
        dataInitializer.run();
        
        // Then - it should not create the initial applications because they already exist
        assertEquals(1, applicationRepository.count(), "It should not create applications when they already exist");
        assertFalse(applicationRepository.existsByClientId("app-readonly"), "It should not create app-readonly");
        assertFalse(applicationRepository.existsByClientId("app-readwrite"), "It should not create app-readwrite");
        assertFalse(applicationRepository.existsByClientId("app-admin"), "It should not create app-admin");
    }
    
    @Test
    void testRun_WithArgs() throws Exception {
        // Given
        assertEquals(0, applicationRepository.count());
        
        // When - invoke run() with arguments
        dataInitializer.run("arg1", "arg2", "arg3");
        
        // Then - it should create the applications independently of the arguments
        assertEquals(3, applicationRepository.count());
    }
    
    @Test
    void testCreateApplications_WhenAppReadOnlyExists() {
        // Given - create app-readonly manually
        Application existingApp = new Application();
        existingApp.setClientId("app-readonly");
        existingApp.setClientSecret(passwordEncoder.encode("existing-secret"));
        existingApp.setName("Existing ReadOnly App");
        existingApp.setScopes(java.util.Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        existingApp.setStatus(true);
        applicationRepository.save(existingApp);
        
        // When - invoke createApplications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Then - it should create only app-readwrite and app-admin
        assertEquals(3, applicationRepository.count());
        
        // Verify that app-readonly was not overwritten
        Optional<Application> appReadOnly = applicationRepository.findByClientIdAndStatusTrue("app-readonly");
        assertTrue(appReadOnly.isPresent());
        assertEquals("Existing ReadOnly App", appReadOnly.get().getName());
        
        // Verify that the other two were created
        assertTrue(applicationRepository.existsByClientId("app-readwrite"));
        assertTrue(applicationRepository.existsByClientId("app-admin"));
    }
    
    @Test
    void testCreateApplications_WhenAppReadWriteExists() {
        // Given - create app-readwrite manually
        Application existingApp = new Application();
        existingApp.setClientId("app-readwrite");
        existingApp.setClientSecret(passwordEncoder.encode("existing-secret"));
        existingApp.setName("Existing ReadWrite App");
        existingApp.setScopes(java.util.Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE));
        existingApp.setStatus(true);
        applicationRepository.save(existingApp);
        
        // When - invoke createApplications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Then - it should create only app-readonly and app-admin
        assertEquals(3, applicationRepository.count());
        
        // Verify that app-readwrite was not overwritten
        Optional<Application> appReadWrite = applicationRepository.findByClientIdAndStatusTrue("app-readwrite");
        assertTrue(appReadWrite.isPresent());
        assertEquals("Existing ReadWrite App", appReadWrite.get().getName());
        
        // Verify that the other two were created
        assertTrue(applicationRepository.existsByClientId("app-readonly"));
        assertTrue(applicationRepository.existsByClientId("app-admin"));
    }
    
    @Test
    void testCreateApplications_WhenAppAdminExists() {
        // Given - create app-admin manually
        Application existingApp = new Application();
        existingApp.setClientId("app-admin");
        existingApp.setClientSecret(passwordEncoder.encode("existing-secret"));
        existingApp.setName("Existing Admin App");
        existingApp.setScopes(java.util.Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE, SecurityConstants.SCOPE_CUSTOMERS_DELETE, SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
        existingApp.setStatus(true);
        applicationRepository.save(existingApp);
        
        // When - invoke createApplications
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Then - it should create only app-readonly and app-readwrite
        assertEquals(3, applicationRepository.count());
        
        // Verify that app-admin was not overwritten
        Optional<Application> appAdmin = applicationRepository.findByClientIdAndStatusTrue("app-admin");
        assertTrue(appAdmin.isPresent());
        assertEquals("Existing Admin App", appAdmin.get().getName());
        
        // Verify that the other two were created
        assertTrue(applicationRepository.existsByClientId("app-readonly"));
        assertTrue(applicationRepository.existsByClientId("app-readwrite"));
    }
    
    @Test
    void testCreateApplications_WhenAllAppsExist() {
        // Given - create the 3 applications manually
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        assertEquals(3, applicationRepository.count());
        
        // When - invoke createApplications again
        ReflectionTestUtils.invokeMethod(Objects.requireNonNull(dataInitializer), "createApplications");
        
        // Then - it should not create duplicates
        assertEquals(3, applicationRepository.count());
    }
}

