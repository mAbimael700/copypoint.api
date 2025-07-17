package com.copypoint.api.domain.payment.validation;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;

    private ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null, null);
    }

    public static ValidationResult success(List<String> warnings) {
        return new ValidationResult(true, null, warnings);
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, null);
    }

    public static ValidationResult failure(String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return new ValidationResult(false, errors, null);
    }

    public String getFirstError() {
        return errors.isEmpty() ? null : errors.getFirst();
    }
}
