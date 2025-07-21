package org.example.backend.exception.customException;

public class ExpertProfileNotFoundException extends RuntimeException {
    public ExpertProfileNotFoundException(String message) {
        super(message);
    }
}
