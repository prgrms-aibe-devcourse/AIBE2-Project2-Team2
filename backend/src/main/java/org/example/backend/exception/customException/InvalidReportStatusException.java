package org.example.backend.exception.customException;

public class InvalidReportStatusException extends RuntimeException {
    public InvalidReportStatusException(String message) {
        super(message);
    }
}