package com.copypoint.api.infra.http.userprofile;

import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import com.copypoint.api.infra.http.userprofile.dto.UserProfileResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class UserProfileController {
    @Autowired
    private UserProfileService userProfileService;

    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(HttpServletRequest request) {
        try {

            // Obtener la autenticación actual
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


            if (authentication == null) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Usuario no autenticado - Authentication null"));
            }



            if (!(authentication.getPrincipal() instanceof UserPrincipal)) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Usuario no autenticado - Principal incorrecto"));
            }

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();


            // Verificar que el usuario esté activo
            if (!userPrincipal.isEnabled()) {

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Usuario inactivo"));
            }
            // Extraer el token del header
            String authHeader = request.getHeader("Authorization");


            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.replace("Bearer ", "");

            }

            if (token == null) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Token no proporcionado"));
            }

            // Obtener el perfil del usuario
            UserProfileResponseDTO userProfile = userProfileService.getUserProfile(userPrincipal.getUser(), token);

            return ResponseEntity.ok(userProfile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }

    // Clase interna para respuestas de error
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
