package com.copypoint.api.application.copypointintegration.exception;

// Excepción específica del dominio
public class CopypointNotFoundException extends RuntimeException {
    public CopypointNotFoundException(String message) {
        super(message);
    }
}
