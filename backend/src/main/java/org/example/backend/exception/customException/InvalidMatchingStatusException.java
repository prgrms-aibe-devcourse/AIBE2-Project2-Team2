package org.example.backend.exception.customException;

public class InvalidMatchingStatusException extends RuntimeException {
    public InvalidMatchingStatusException(String message) {
        super(message);
    }
}
