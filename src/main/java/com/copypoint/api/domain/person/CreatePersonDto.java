package com.copypoint.api.domain.person;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePersonDto(
        @NotBlank(message = "First name is mandatory")
        @Size(min = 2, max = 50, message = "First name must have between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(min = 2, max = 50, message = "Last name must have between 2 and 50 characters")
        String lastName,

        @Pattern(
                regexp = "^[+]?[0-9]{10,15}$",
                message = "Phone number must have between 10 and 15 digits"
        )
        String phoneNumber
) {

}
