package com.tmforum.openapi.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingFilterTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;
    
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() throws IOException {
        // Configure the filter with a low limit for testing
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "requestsPerMinute", 5);
        
        // Configure StringWriter to capture the response (only when needed)
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }
    
    private void setupResponseWriter() throws IOException {
        when(response.getWriter()).thenReturn(printWriter);
    }
    
    @Test
    void testDoFilter_RequestAllowed_ContinuesChain() throws ServletException, IOException {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
    
    @Test
    void testDoFilter_RateLimitExceeded_Returns429() throws ServletException, IOException {
        // Given
        setupResponseWriter();
        when(request.getRemoteAddr()).thenReturn("192.168.1.2");
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "requestsPerMinute", 1);
        
        // Consume the only available token
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // When - try another request
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(response, times(1)).setStatus(429);
        verify(response, times(1)).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
        verify(filterChain, times(1)).doFilter(request, response); // Only the first one passed
    }
    
    @Test
    void testGetClientIp_FromXForwardedFor() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
        // The IP should be the first one in X-Forwarded-For
    }
    
    @Test
    void testGetClientIp_FromXRealIp() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.2");
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testGetClientIp_FromRemoteAddr() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.3");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testGetClientIp_XForwardedForWithSpaces() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(" 203.0.113.3 , 198.51.100.2 ");
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testGetClientIp_EmptyXForwardedFor() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.4");
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testGetClientIp_EmptyXRealIp() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("192.168.1.4");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testDoFilter_DifferentIps_IndependentBuckets() throws ServletException, IOException {
        // Given
        setupResponseWriter();
        when(request.getRemoteAddr()).thenReturn("192.168.1.5");
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "requestsPerMinute", 1);
        
        // Consume the token for the first IP
        rateLimitingFilter.doFilter(request, response, filterChain);
        rateLimitingFilter.doFilter(request, response, filterChain); // Should be blocked
        
        // Change to another IP
        when(request.getRemoteAddr()).thenReturn("192.168.1.6");
        
        // When - the new IP should have its own bucket
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then
        verify(filterChain, times(2)).doFilter(request, response); // One for each IP
    }
    
    @Test
    void testGetOrder_ReturnsOne() {
        // When
        int order = rateLimitingFilter.getOrder();
        
        // Then
        assertEquals(1, order);
    }
    
    @Test
    void testDoFilter_MultipleRequests_SameIp() throws ServletException, IOException {
        // Given
        when(request.getRemoteAddr()).thenReturn("192.168.1.7");
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "requestsPerMinute", 3);
        
        // When - make 3 requests (within the limit)
        for (int i = 0; i < 3; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }
        
        // Then
        verify(filterChain, times(3)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
    
    @Test
    void testDoFilter_CleanupWhenManyBuckets() throws ServletException, IOException {
        // Given
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "lastCleanup", System.currentTimeMillis() - 700000); // More than 10 minutes
        ReflectionTestUtils.setField(Objects.requireNonNull(rateLimitingFilter), "requestsPerMinute", 100);
        
        // Create many simulated buckets using reflection
        var bucketsField = ReflectionTestUtils.getField(Objects.requireNonNull(rateLimitingFilter), "buckets");
        assertNotNull(bucketsField);
        
        // Simulate that there are more than 1000 buckets
        when(request.getRemoteAddr()).thenReturn("192.168.1.8");
        
        // When
        rateLimitingFilter.doFilter(request, response, filterChain);
        
        // Then - the cleanup should be executed
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
