package com.copypoint.api.domain.user;

import com.copypoint.api.domain.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Page<UserDTO> getAll(Pageable pageable) {
        return userRepository
                .findByStatus(UserStatus.ACTIVE, pageable)
                .map(UserDTO::new);
    }

    public UserDTO create(CreateUserDto createUserDto) {
        Person newPerson = Person
                .builder()
                .firstName(createUserDto.personInfo().firstName())
                .lastName(createUserDto.personInfo().lastName())
                .phoneNumber(createUserDto.personInfo().phoneNumber())
                .build();

        User newUser =  User
                .builder()
                .email(createUserDto.email())
                .password(createUserDto.password())
                .personalInformation(newPerson)
                .status(UserStatus.ACTIVE)
                .build();

        var saved = userRepository.save(newUser);
        return new UserDTO(saved);
    }
}
