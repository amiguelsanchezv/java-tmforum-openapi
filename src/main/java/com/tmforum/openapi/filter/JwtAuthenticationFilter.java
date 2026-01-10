package com.tmforum.openapi.filter;

import com.tmforum.openapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.Ordered;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter implements Ordered {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        
        // Ignore public endpoints
        String path = request.getRequestURI();
        String servletPath = request.getServletPath();
        
        // Check both full path and servlet path
        if (path.startsWith("/api/auth/") || 
            path.startsWith("/auth/") ||
            servletPath.startsWith("/auth/") ||
            path.startsWith("/api-docs/") || 
            path.startsWith("/swagger-ui/") ||
            path.equals("/api/swagger-ui.html") ||
            path.equals("/swagger-ui.html")) {
            chain.doFilter(request, response);
            return;
        }
        
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        List<String> scopes = null;
        
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            
            // Only process if token is not empty and has correct format
            if (StringUtils.hasText(jwt) && jwt.split("\\.").length == 3) {
                try {
                    username = jwtUtil.extractUsername(jwt);
                    scopes = jwtUtil.extractScopes(jwt);
                } catch (Exception e) {
                    logger.debug("Error processing JWT: " + e.getMessage());
                    // Do not set authentication if there is an error
                }
            }
        }
        
        if (username != null && scopes != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtil.validateToken(jwt)) {
                    // Convert scopes to authorities
                    List<SimpleGrantedAuthority> authorities = scopes.stream()
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .collect(Collectors.toList());
                    
                    UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.debug("Error validating JWT token: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public int getOrder() {
        return 2;
    }
}

