package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.user.dto.UserCreationDTO;
import com.copypoint.api.domain.user.dto.UserDTO;
import com.copypoint.api.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAll(Pageable pageable) {
        Page<UserDTO> users = userService.getAll(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody @Valid UserCreationDTO userCreationDTO) {
        UserDTO user = userService.create(userCreationDTO);
        URI location = URI.create("/users/" + user.id()); // Asumiendo que UserDTO tiene getId()
        return ResponseEntity.created(location).body(user);
    }

}
