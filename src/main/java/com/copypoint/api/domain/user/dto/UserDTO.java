package com.copypoint.api.domain.user.dto;

import com.copypoint.api.domain.person.dto.PersonDto;
import com.copypoint.api.domain.user.User;

import java.time.LocalDateTime;

public record UserDTO(
        Long id,
        String username,
        String email,
        PersonDto personalInfo,
        LocalDateTime creationDate
) {
    public UserDTO(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                new PersonDto(user.getPersonalInformation()),
                user.getCreationDate()
        );
    }
}
