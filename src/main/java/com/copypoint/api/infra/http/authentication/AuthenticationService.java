package com.copypoint.api.infra.http.authentication;

import com.copypoint.api.domain.permission.PermissionService;
import com.copypoint.api.domain.user.UserRepository;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        // Obtener módulos y roles del usuario
        PermissionService.UserPermissionInfo permissionInfo =
                permissionService.getUserPermissionInfo(user);

        return new UserPrincipal(user, permissionInfo.getModules(), permissionInfo.getRoles());
    }
}
