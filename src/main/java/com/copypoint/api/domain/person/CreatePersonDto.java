package com.copypoint.api.domain.person;

public record CreatePersonDto(
        String firstName,
        String lastName,
        String phoneNumber
) {

}
