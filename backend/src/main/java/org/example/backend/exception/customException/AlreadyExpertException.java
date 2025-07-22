package org.example.backend.exception.customException;

public class AlreadyExpertException extends RuntimeException {
    public AlreadyExpertException(String message) {
        super(message);
    }
}
