package org.example.backend.exception.customException;

public class InvalidPortfolioImageException extends RuntimeException {
    public InvalidPortfolioImageException(String message) {
        super(message);
    }
}
