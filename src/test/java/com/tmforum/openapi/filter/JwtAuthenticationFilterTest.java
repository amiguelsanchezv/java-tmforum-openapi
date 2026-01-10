package com.tmforum.openapi.filter;

import com.tmforum.openapi.config.SecurityConstants;
import com.tmforum.openapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private JwtAuthenticationFilter filter;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }
    
    /**
     * Helper method to call doFilterInternal with null-safety checks.
     * This satisfies the static analyzer's null-safety requirements.
     */
    private void doFilterInternalSafe() throws Exception {
        filter.doFilterInternal(
            Objects.requireNonNull(request),
            Objects.requireNonNull(response),
            Objects.requireNonNull(filterChain)
        );
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_AuthPath() throws Exception {
        request.setRequestURI("/api/auth/login");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).extractUsername(anyString());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_AuthPathWithoutApi() throws Exception {
        request.setRequestURI("/auth/login");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_ServletPath() throws Exception {
        request.setServletPath("/auth/login");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_ApiDocs() throws Exception {
        request.setRequestURI("/api-docs/v3/api-docs");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_SwaggerUI() throws Exception {
        request.setRequestURI("/swagger-ui/index.html");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_SwaggerUIHtml() throws Exception {
        request.setRequestURI("/swagger-ui.html");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_PublicEndpoint_SwaggerUIHtmlWithApi() throws Exception {
        request.setRequestURI("/api/swagger-ui.html");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_ValidToken() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        when(jwtUtil.extractUsername(validJwt)).thenReturn("app-admin");
        when(jwtUtil.extractScopes(validJwt)).thenReturn(Arrays.asList("customers:read", "customers:write"));
        when(jwtUtil.validateToken(validJwt)).thenReturn(true);
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("app-admin", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(SecurityConstants.SCOPE_CUSTOMERS_READ)));
    }
    
    @Test
    void testDoFilterInternal_InvalidToken() throws Exception {
        request.setRequestURI("/api/customers");
        String invalidJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + invalidJwt);
        
        lenient().when(jwtUtil.extractUsername(invalidJwt)).thenReturn("app-admin");
        lenient().when(jwtUtil.extractScopes(invalidJwt)).thenReturn(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        when(jwtUtil.validateToken(invalidJwt)).thenReturn(false);
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_NoToken() throws Exception {
        request.setRequestURI("/api/customers");
        // Without Authorization header
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).extractUsername(anyString());
    }
    
    @Test
    void testDoFilterInternal_MalformedToken() throws Exception {
        request.setRequestURI("/api/customers");
        request.addHeader("Authorization", "Bearer not.a.valid.jwt.token.format");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_TokenWithWrongFormat() throws Exception {
        request.setRequestURI("/api/customers");
        request.addHeader("Authorization", "Bearer invalid");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_EmptyToken() throws Exception {
        request.setRequestURI("/api/customers");
        request.addHeader("Authorization", "Bearer ");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_NoBearerPrefix() throws Exception {
        request.setRequestURI("/api/customers");
        request.addHeader("Authorization", "Token valid-token");
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_ExceptionExtractingUsername() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        when(jwtUtil.extractUsername(validJwt)).thenThrow(new RuntimeException("Token expired"));
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_ExceptionValidatingToken() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        when(jwtUtil.extractUsername(validJwt)).thenReturn("app-admin");
        when(jwtUtil.extractScopes(validJwt)).thenReturn(Arrays.asList(SecurityConstants.SCOPE_CUSTOMERS_READ));
        when(jwtUtil.validateToken(validJwt)).thenThrow(new RuntimeException("Token invalid"));
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void testDoFilterInternal_AlreadyAuthenticated() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        // Simulate that authentication already exists
        org.springframework.security.core.Authentication existingAuth = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existing-user", null, Arrays.asList());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        // Should not change existing authentication
        assertEquals("existing-user", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(jwtUtil, never()).validateToken(anyString());
    }
    
    @Test
    void testDoFilterInternal_MultipleScopes() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        List<String> scopes = Arrays.asList("customers:read", "customers:write", "customers:delete", "customers:admin");
        when(jwtUtil.extractUsername(validJwt)).thenReturn("app-admin");
        when(jwtUtil.extractScopes(validJwt)).thenReturn(scopes);
        when(jwtUtil.validateToken(validJwt)).thenReturn(true);
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(4, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
        assertTrue(SecurityContextHolder.getContext().getAuthentication()
            .getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(SecurityConstants.SCOPE_CUSTOMERS_ADMIN)));
    }
    
    @Test
    void testDoFilterInternal_ExceptionExtractingScopes() throws Exception {
        request.setRequestURI("/api/customers");
        String validJwt = "header.payload.signature";
        request.addHeader("Authorization", "Bearer " + validJwt);
        
        when(jwtUtil.extractUsername(validJwt)).thenReturn("app-admin");
        when(jwtUtil.extractScopes(validJwt)).thenThrow(new RuntimeException("Error extracting scopes"));
        
        doFilterInternalSafe();
        
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

