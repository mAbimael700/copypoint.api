package com.copypoint.api.infra.http.authentication;

import com.copypoint.api.domain.permission.PermissionService;
import com.copypoint.api.domain.user.repository.UserRepository;
import com.copypoint.api.infra.http.token.TokenService;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Obtener el token del header
        var authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            var token = authHeader.replace("Bearer ", "");
            var email = tokenService.getSubject(token); // extract username

            if (email != null) {

                // Token valido
                var userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    var user = userOptional.get();
                    // Obtener toda la información de permisos en una sola consulta
                    PermissionService.UserPermissionInfo permissionInfo =
                            permissionService.getUserPermissionInfo(user);
                    // Crear UserPrincipal con permisos completos
                    var userPrincipal = new UserPrincipal(user,
                            permissionInfo.getModules(), permissionInfo.getRoles());

                    var authentication = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            userPrincipal.getAuthorities()); // Forzamos un inicio de sesion

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
