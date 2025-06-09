package com.copypoint.api.infra.http.authentication;

import com.copypoint.api.infra.http.token.JWTTokenDto;
import com.copypoint.api.infra.http.token.TokenService;
import com.copypoint.api.infra.http.userPrincipal.UserPrincipal;
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

@RestController
@RequestMapping("/api")
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity authenticateUser(@RequestBody @Valid AuthenticationDto authenticationDto) {
        try {
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    authenticationDto.email(),
                    authenticationDto.password()
            );

            var authenticatedUser = authenticationManager.authenticate(authToken);
            UserPrincipal userPrincipal = (UserPrincipal) authenticatedUser.getPrincipal();
            var JWTToken = tokenService.generateToken(userPrincipal.getUser());

            return ResponseEntity.ok(new JWTTokenDto(JWTToken));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}
