package com.copypoint.api.domain.user;
import com.copypoint.api.domain.person.Person;
import com.copypoint.api.domain.person.PersonRepository;
import com.copypoint.api.domain.user.dto.CreateUserDto;
import com.copypoint.api.domain.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PersonRepository personRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
                .password(passwordEncoder.encode(createUserDto.password()))
                .personalInformation(newPerson)
                .status(UserStatus.ACTIVE)
                .creationDate(LocalDateTime.now())
                .build();

        personRepository.save(newPerson);
        var saved = userRepository.save(newUser);
        return new UserDTO(saved);
    }
}
