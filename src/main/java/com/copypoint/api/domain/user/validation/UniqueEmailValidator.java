package com.copypoint.api.domain.user.validation;

import com.copypoint.api.domain.user.repository.UserRepository;
import com.copypoint.api.domain.user.validation.annotation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        if (email == null) {
            return true; // Let @NotNull handle null values
        }
        return !userRepository.existsByEmail(email);
    }
}
