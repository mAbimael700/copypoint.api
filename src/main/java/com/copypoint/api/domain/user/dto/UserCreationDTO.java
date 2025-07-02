package com.copypoint.api.domain.user.dto;

import com.copypoint.api.domain.person.dto.PersonCreationDto;
import com.copypoint.api.domain.user.validation.annotation.UniqueEmail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record UserCreationDTO(
        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email format is not valid")
        @Size(max = 50, message = "Email can not more than 50 characters")
        @UniqueEmail // Custom validation
        String email,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, max = 100, message = "Password must between 8 and 100 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d])[\\S]{8,}$",
                message = "Password must have: 1 lowercase, 1 uppercase, 1 number and 1 special character"
        )
        String password,

        @Size(min = 2, max = 100, message = "Username must between 8 and 100 characters")
        String username,

        @NotNull(message = "Personal information is mandatory")
        @Valid
        PersonCreationDto personalInfo
) {
}
