package com.tmforum.openapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import com.tmforum.openapi.config.SecurityConstants;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    private static final String SECRET = "MyVerySecureSecretKeyForJWTThatMustBeVeryLongForHS512AtLeast256Bits";
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(Objects.requireNonNull(jwtUtil), "secret", SECRET);
        ReflectionTestUtils.setField(Objects.requireNonNull(jwtUtil), "expiration", 3600L); // 1 hour in seconds, 3600 seconds
    }
    
    @Test
    void testGenerateToken() {
        // When
        String token = jwtUtil.generateToken("test-user", SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        
        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }
    
    @Test
    void testExtractUsername() {
        // Given
        String token = jwtUtil.generateToken("test-user", SecurityConstants.SCOPE_CUSTOMERS_READ);
        
        // When
        String username = jwtUtil.extractUsername(token);
        
        // Then
        assertEquals("test-user", username);
    }
    
    @Test
    void testExtractScopes() {
        // Given
        String token = jwtUtil.generateToken("test-user", SecurityConstants.SCOPE_CUSTOMERS_READ, SecurityConstants.SCOPE_CUSTOMERS_WRITE);
        
        // When
        List<String> scopes = jwtUtil.extractScopes(token);
        
        // Then
        assertNotNull(scopes);
        assertEquals(2, scopes.size());
        assertTrue(scopes.contains(SecurityConstants.SCOPE_CUSTOMERS_READ));
        assertTrue(scopes.contains(SecurityConstants.SCOPE_CUSTOMERS_WRITE));
    }
    
    @Test
    void testValidateToken_Valid() {
        // Given
        String token = jwtUtil.generateToken("test-user", SecurityConstants.SCOPE_CUSTOMERS_READ);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_Invalid() {
        // Given
        String tokenInvalid = "token.invalid.123";
        
        // When
        Boolean isValid = jwtUtil.validateToken(tokenInvalid);
        
        // Then
        assertFalse(isValid);
    }
    
    @Test
    void testExtractExpiration() {
        // Given
        String token = jwtUtil.generateToken("test-user", SecurityConstants.SCOPE_CUSTOMERS_READ);
        
        // When
        java.util.Date expiration = jwtUtil.extractExpiration(token);
        
        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new java.util.Date()));
    }
    
    @Test
    void testGenerateToken_WithUserDetails() {
        // Given
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ,
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_WRITE
                );
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        // When
        String token = jwtUtil.generateToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
        
        // Verify that the username is extracted correctly
        String username = jwtUtil.extractUsername(token);
        assertEquals("test-user", username);
        
        // Verify that the token is valid
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void testGenerateToken_WithUserDetails_EmptyAuthorities() {
        // Given
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user-empty";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        // When
        String token = jwtUtil.generateToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertEquals("test-user-empty", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void testGenerateToken_WithUserDetails_MultipleAuthorities() {
        // Given
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ,
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_WRITE,
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_DELETE,
                    (GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_ADMIN
                );
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "admin-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        // When
        String token = jwtUtil.generateToken(userDetails);
        
        // Then
        assertNotNull(token);
        assertEquals("admin-user", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token));
    }
    
    @Test
    void testValidateToken_WithUserDetails_Valid() {
        // Given
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ);
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        String token = jwtUtil.generateToken(userDetails);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        // Then
        assertTrue(isValid);
    }
    
    @Test
    void testValidateToken_WithUserDetails_DifferentUsername() {
        // Given
        UserDetails userDetails1 = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ);
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "user1";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        UserDetails userDetails2 = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ);
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "user2";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        String token = jwtUtil.generateToken(userDetails1);
        
        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails2);
        
        // Then
        assertFalse(isValid, "The token should not be valid for a different user");
    }
    
    @Test
    void testValidateToken_WithUserDetails_ExpiredToken() throws Exception {
        // Given - Create a token with a very short expiration
        ReflectionTestUtils.setField(Objects.requireNonNull(jwtUtil), "expiration", 1L); // 1 second
        
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ);
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        String token = jwtUtil.generateToken(userDetails);
        
        // Wait for the token to expire
        Thread.sleep(1100); // Wait more than 1 second
        
        // When - validateToken with UserDetails can throw an exception if the token is expired
        // because it tries to extract the username from the expired token
        Boolean isValid = false;
        try {
            isValid = jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            // Expected: when the token is expired, extractUsername can throw an exception
            isValid = false;
        }
        
        // Then
        assertFalse(isValid, "The expired token should not be valid");
        
        // Restore original expiration
        ReflectionTestUtils.setField(Objects.requireNonNull(jwtUtil), "expiration", 3600L);
    }
    
    @Test
    void testValidateToken_ExceptionHandling() {
        // Given - Completely invalid token
        String invalidToken = "completely.invalid.token";
        
        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid, "An invalid token should return false");
    }
    
    @Test
    void testExtractScopes_WhenScopesIsNotList() {
        // Given - Token generated with UserDetails (uses "authorities" not "scopes")
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of((GrantedAuthority) () -> SecurityConstants.SCOPE_CUSTOMERS_READ);
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        String token = jwtUtil.generateToken(userDetails);
        
        // When - Try to extract scopes from a token that has "authorities" instead of "scopes"
        List<String> scopes = jwtUtil.extractScopes(token);
        
        // Then - Should return an empty list because the token does not have "scopes"
        assertNotNull(scopes);
        assertTrue(scopes.isEmpty(), "When the token does not have scopes, it should return an empty list");
    }
    
    @Test
    void testExtractScopes_WhenScopesIsNull() {
        // Given - Token without scopes (generated with UserDetails that does not have scopes in claims)
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
            
            @Override
            public String getPassword() {
                return "password";
            }
            
            @Override
            public String getUsername() {
                return "test-user";
            }
            
            @Override
            public boolean isAccountNonExpired() {
                return true;
            }
            
            @Override
            public boolean isAccountNonLocked() {
                return true;
            }
            
            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }
            
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        
        String token = jwtUtil.generateToken(userDetails);
        
        // When
        List<String> scopes = jwtUtil.extractScopes(token);
        
        // Then
        assertNotNull(scopes);
        assertTrue(scopes.isEmpty());
    }
}

