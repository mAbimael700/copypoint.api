package com.copypoint.api.domain.person;

public record PersonDto(
        String firstName,
        String lastName,
        String phoneNumber
) {

    public PersonDto(Person person) {
        this(
                person.getFirstName(),
                person.getLastName(),
                person.getPhoneNumber()
        );
    }
}
