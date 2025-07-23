package org.example.backend.exception.customException;

public class NotExpertException extends RuntimeException {
    public NotExpertException(String message) {
        super(message);
    }
}
