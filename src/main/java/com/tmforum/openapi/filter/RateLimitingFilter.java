package com.tmforum.openapi.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter for rate limiting using Bucket4j
 * Limits requests per IP
 */
@Component
@ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimitingFilter implements Filter, Ordered {
    
    // Clean up old buckets periodically to avoid memory leak
    private static final long CLEANUP_INTERVAL_MS = 600000; // 10 minutes
    private long lastCleanup = System.currentTimeMillis();
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // Bucket per IP
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Value("${rate.limit.requests-per-minute:100}")
    private int requestsPerMinute;
    
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, 
                          jakarta.servlet.ServletResponse response, 
                          FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Clean up old buckets periodically
        long now = System.currentTimeMillis();
        if (now - lastCleanup > CLEANUP_INTERVAL_MS) {
            // Clean up buckets from IPs that haven't made requests recently
            // This prevents memory leaks in case of many different IPs
            if (buckets.size() > 1000) {
                buckets.clear();
                logger.debug("Cleaned up rate limiting buckets");
            }
            lastCleanup = now;
        }
        
        // Get client IP
        String clientIp = getClientIp(httpRequest);
        
        // Get or create bucket for this IP
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::createBucket);
        
        // Check if tokens are available
        if (bucket.tryConsume(1)) {
            // Tokens available, continue with request
            chain.doFilter(request, response);
        } else {
            // Rate limit exceeded (429 Too Many Requests)
            logger.warn("Rate limit exceeded for IP: {}", clientIp);
            httpResponse.setStatus(429); // SC_TOO_MANY_REQUESTS
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
        }
    }
    
    private Bucket createBucket(String ip) {
        // Create bucket with configurable capacity of requests per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(requestsPerMinute)
                .refillIntervally(requestsPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
}

