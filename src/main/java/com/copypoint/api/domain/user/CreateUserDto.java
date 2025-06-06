package com.copypoint.api.domain.user;

import com.copypoint.api.domain.person.CreatePersonDto;

public record CreateUserDto(
        String email,
        String password,
        CreatePersonDto personInfo
) {
}
