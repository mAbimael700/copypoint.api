package com.copypoint.api.infra.http.userprincipal;

import com.copypoint.api.domain.user.User;
import com.copypoint.api.domain.user.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {
    private final User user;
    private final Set<String> userModules;
    private final Set<String> userRoles;

    public UserPrincipal(User user, Set<String> userModules, Set<String> userRoles) {
        this.user = user;
        this.userModules = userModules;
        this.userRoles = userRoles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Crear authorities basados en los módulos y roles del usuario
        List<GrantedAuthority> authorities = userModules.stream()
                .map(module -> new SimpleGrantedAuthority("MODULE_" + module.toUpperCase()))
                .collect(Collectors.toList());

        // Agregar authorities basados en roles
        List<GrantedAuthority> roleAuthorities = userRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());

        authorities.addAll(roleAuthorities);

        // Agregar el rol básico de usuario
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !(user.getStatus().equals(UserStatus.EXPIRED));
    }

    @Override
    public boolean isAccountNonLocked() {
        return !(user.getStatus().equals(UserStatus.LOCKED) || user.getStatus().equals(UserStatus.BLOCKED));
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus().equals(UserStatus.ACTIVE);
    }

    // Métodos de conveniencia para verificar permisos
    public boolean hasModule(String moduleName) {
        return userModules.contains(moduleName);
    }

    public boolean hasRole(String roleName) {
        return userRoles.contains(roleName);
    }

    public boolean hasAnyModule(String... moduleNames) {
        return userModules.stream().anyMatch(module ->
                List.of(moduleNames).contains(module));
    }

    public boolean hasAnyRole(String... roleNames) {
        return userRoles.stream().anyMatch(role ->
                List.of(roleNames).contains(role));
    }
}
