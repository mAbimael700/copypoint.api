package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.user.dto.UserCreationDTO;
import com.copypoint.api.domain.user.dto.UserDTO;
import com.copypoint.api.domain.user.service.UserService;
import com.copypoint.api.infra.http.authentication.dto.AuthenticationDto;
import com.copypoint.api.infra.http.token.TokenDTO;
import com.copypoint.api.infra.http.token.TokenService;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;


@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity authenticateUser(@RequestBody @Valid AuthenticationDto authenticationDto) {
        try {
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    authenticationDto.email(),
                    authenticationDto.password()
            );

            var authenticatedUser = authenticationManager.authenticate(authToken);
            UserPrincipal userPrincipal = (UserPrincipal) authenticatedUser.getPrincipal();
            var JWTToken = tokenService.generateToken(userPrincipal.getUser());

            return ResponseEntity.ok(new TokenDTO(JWTToken));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserDTO> create(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        UserDTO user = userService.create(userCreationDTO);
        URI location = URI.create("/users/" + user.id()); // Asumiendo que UserDTO tiene getId()
        return ResponseEntity.created(location).body(user);
    }
}
