package com.copypoint.api.infra.http.authorization.filter;

import com.copypoint.api.domain.permission.PermissionService;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthorizationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Autowired
    private PermissionService permissionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserPrincipal) {

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            String method = request.getMethod();
            String path = request.getRequestURI();

            // Verificar permisos
            boolean hasPermission = permissionService.hasPermission(userPrincipal, method, path);

            logger.info("Permission check result: {}", hasPermission);

            if (!hasPermission) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\":\"No tienes permisos para acceder a este recurso\"}");
                response.setContentType("application/json");
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // No filtrar endpoints públicos
        return path.equals("/api/auth/sign-in") ||
                path.equals("/api/auth/sign-up") ||
                path.equals("/api/auth/my-profile") ||
                path.equals("/api/auth/logout") ||
                path.equals("/api/payment-methods") ||
                (path.equals("/api/users") && "POST".equals(request.getMethod())) ||
                (path.equals("/api/stores") && "POST".equals(request.getMethod())) ||
                (path.equals("/api/stores") && "GET".equals(request.getMethod())) ||
                "OPTIONS".equals(request.getMethod());
    }
}
