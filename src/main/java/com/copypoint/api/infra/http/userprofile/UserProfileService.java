package com.copypoint.api.infra.http.userprofile;

import com.copypoint.api.domain.person.Person;
import com.copypoint.api.domain.user.User;
import com.copypoint.api.domain.user.repository.UserRepository;
import com.copypoint.api.infra.http.token.TokenService;
import com.copypoint.api.infra.http.userprofile.dto.UserProfileResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class UserProfileService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    public UserProfileResponseDTO getUserProfile(User user, String token) {


        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + user.getId()));

        // Extraer la fecha de expiración del token

        Long tokenExpiration = getTokenExpiration(token);

        // Obtener el nombre del usuario desde la información personal

        String userName = getUserDisplayName(freshUser);


        return new UserProfileResponseDTO(
                freshUser.getId(),
                userName,
                freshUser.getEmail(),
                tokenExpiration
        );

    }

    private Long getTokenExpiration(String token) {

        return tokenService.getExpirationDate(token);

    }

    private String getUserDisplayName(User user) {

        if (user.getPersonalInformation() != null) {
            Person person = user.getPersonalInformation();
            // Ajusta según los campos reales de Person
            String firstName = person.getFirstName() != null ? person.getFirstName() : "";
            String lastName = person.getLastName() != null ? person.getLastName() : "";

            if (!firstName.isEmpty() || !lastName.isEmpty()) {
                return (firstName + " " + lastName).trim();
            }
        }


        // Fallback al username o email si no hay información personal
        return user.getUsername() != null ? user.getUsername() : user.getEmail();
    }
}
