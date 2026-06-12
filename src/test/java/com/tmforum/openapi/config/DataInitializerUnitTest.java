package com.tmforum.openapi.config;

import com.tmforum.openapi.model.Application;
import com.tmforum.openapi.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import com.tmforum.openapi.config.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataInitializerUnitTest {
    
    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private DataInitializer dataInitializer;
    
    @BeforeEach
    void setUp() {
        // Configure default behavior of the passwordEncoder
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        org.springframework.test.util.ReflectionTestUtils.setField(dataInitializer, "readonlySecret", "secret-readonly");
        org.springframework.test.util.ReflectionTestUtils.setField(dataInitializer, "readwriteSecret", "secret-readwrite");
        org.springframework.test.util.ReflectionTestUtils.setField(dataInitializer, "adminSecret", "secret-admin");
    }
    
    @Test
    @SuppressWarnings("null")
    void testRun_WhenNoApplications_CreatesInitialApplications() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - it would call cleanupDuplicates and createApplications
        verify(applicationRepository, times(1)).findAll(); // Called once in cleanupDuplicates
        verify(applicationRepository, times(3)).save(any(Application.class));
        verify(applicationRepository, times(1)).existsByClientId("app-readonly");
        verify(applicationRepository, times(1)).existsByClientId("app-readwrite");
        verify(applicationRepository, times(1)).existsByClientId("app-admin");
    }
    
    @Test
    @SuppressWarnings("null")
    void testRun_WhenApplicationsExist_DoesNotCreate() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(true);
        
        // When
        dataInitializer.run();
        
        // Then - it should call cleanupDuplicates but not create applications since they exist
        verify(applicationRepository, times(1)).findAll(); // Called once in cleanupDuplicates
        verify(applicationRepository, never()).save(any(Application.class));
        verify(applicationRepository, times(1)).existsByClientId("app-readonly");
        verify(applicationRepository, times(1)).existsByClientId("app-readwrite");
        verify(applicationRepository, times(1)).existsByClientId("app-admin");
    }
    
    @Test
    @SuppressWarnings("null")
    void testRun_WithArgs() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When - invoke run() with arguments
        dataInitializer.run("arg1", "arg2", "arg3");
        
        // Then - it should create the applications independently of the arguments
        verify(applicationRepository, times(1)).findAll(); // Called once in cleanupDuplicates
        verify(applicationRepository, times(3)).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_CreatesAllThreeApps() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - verify that the 3 applications were created
        verify(applicationRepository, times(3)).save(any(Application.class));
        
        // Verify that existsByClientId was called for each application
        verify(applicationRepository, times(1)).existsByClientId("app-readonly");
        verify(applicationRepository, times(1)).existsByClientId("app-readwrite");
        verify(applicationRepository, times(1)).existsByClientId("app-admin");
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_WhenAppReadOnlyExists() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId("app-readonly")).thenReturn(true);
        when(applicationRepository.existsByClientId("app-readwrite")).thenReturn(false);
        when(applicationRepository.existsByClientId("app-admin")).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - it should create only app-readwrite and app-admin
        verify(applicationRepository, times(2)).save(any(Application.class));
        verify(applicationRepository, never()).save(argThat(app -> "app-readonly".equals(app.getClientId())));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_WhenAppReadWriteExists() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId("app-readonly")).thenReturn(false);
        when(applicationRepository.existsByClientId("app-readwrite")).thenReturn(true);
        when(applicationRepository.existsByClientId("app-admin")).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - it should create only app-readonly and app-admin
        verify(applicationRepository, times(2)).save(any(Application.class));
        verify(applicationRepository, never()).save(argThat(app -> "app-readwrite".equals(app.getClientId())));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_WhenAppAdminExists() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId("app-readonly")).thenReturn(false);
        when(applicationRepository.existsByClientId("app-readwrite")).thenReturn(false);
        when(applicationRepository.existsByClientId("app-admin")).thenReturn(true);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - it should create only app-readonly and app-readwrite
        verify(applicationRepository, times(2)).save(any(Application.class));
        verify(applicationRepository, never()).save(argThat(app -> "app-admin".equals(app.getClientId())));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_WhenAllAppsExist() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(true);
        
        // When
        dataInitializer.run();
        
        // Then - it should not create any application
        verify(applicationRepository, never()).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_AppReadOnlyProperties() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            // Verify properties of app-readonly
            if ("app-readonly".equals(app.getClientId())) {
                assertEquals("Read-Only Application", app.getName());
                assertEquals("Application with read-only permissions", app.getDescription());
                assertEquals(1, app.getScopes().size());
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
                assertTrue(app.getStatus());
                assertNotNull(app.getCreationDate());
                assertNotNull(app.getUpdateDate());
            }
            return app;
        });
        
        // When
        dataInitializer.run();
        
        // Then
        verify(applicationRepository, times(3)).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_AppReadWriteProperties() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            // Verify properties of app-readwrite
            if ("app-readwrite".equals(app.getClientId())) {
                assertEquals("Read/Write Application", app.getName());
                assertEquals("Application with read and write permissions", app.getDescription());
                assertEquals(2, app.getScopes().size());
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
                assertTrue(app.getStatus());
                assertNotNull(app.getCreationDate());
                assertNotNull(app.getUpdateDate());
            }
            return app;
        });
        
        // When
        dataInitializer.run();
        
        // Then
        verify(applicationRepository, times(3)).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_AppAdminProperties() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application app = invocation.getArgument(0);
            // Verify properties of app-admin
            if ("app-admin".equals(app.getClientId())) {
                assertEquals("Administrator Application", app.getName());
                assertEquals("Application with all permissions", app.getDescription());
                assertEquals(4, app.getScopes().size());
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_DELETE));
                assertTrue(app.getScopes().contains(SecurityConstants.SCOPE_CUSTOMERS_ADMIN));
                assertTrue(app.getStatus());
                assertNotNull(app.getCreationDate());
                assertNotNull(app.getUpdateDate());
            }
            return app;
        });
        
        // When
        dataInitializer.run();
        
        // Then
        verify(applicationRepository, times(3)).save(any(Application.class));
    }
    
    @Test
    @SuppressWarnings("null")
    void testCreateApplications_PasswordEncoding() throws Exception {
        // Given
        when(applicationRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(applicationRepository.existsByClientId(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        dataInitializer.run();
        
        // Then - verify that the passwords were encoded
        verify(passwordEncoder, times(1)).encode("secret-readonly");
        verify(passwordEncoder, times(1)).encode("secret-readwrite");
        verify(passwordEncoder, times(1)).encode("secret-admin");
    }
}

