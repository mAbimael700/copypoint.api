package com.copypoint.api.domain.user;

import com.copypoint.api.domain.person.Person;
import com.copypoint.api.domain.person.PersonRepository;
import com.copypoint.api.domain.user.dto.UserCreationDTO;
import com.copypoint.api.domain.user.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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

    public UserDTO create(UserCreationDTO userCreationDTO) {
        Person newPerson = Person
                .builder()
                .firstName(userCreationDTO.personInfo().firstName())
                .lastName(userCreationDTO.personInfo().lastName())
                .phoneNumber(userCreationDTO.personInfo().phoneNumber())
                .build();

        User newUser = User
                .builder()
                .email(userCreationDTO.email())
                .password(passwordEncoder.encode(userCreationDTO.password()))
                .personalInformation(newPerson)
                .status(UserStatus.ACTIVE)
                .creationDate(LocalDateTime.now())
                .build();

        personRepository.save(newPerson);
        var saved = userRepository.save(newUser);
        return new UserDTO(saved);
    }

    public Optional<User> getById(Long userId) {
        return userRepository.findById(userId);
    }
}
