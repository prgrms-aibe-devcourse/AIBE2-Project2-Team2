package org.example.backend.exception.customException;

public class NoContentPermissionException extends RuntimeException {
    public NoContentPermissionException(String message) {
        super(message);
    }
} 