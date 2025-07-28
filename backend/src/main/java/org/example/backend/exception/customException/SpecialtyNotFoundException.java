package org.example.backend.exception.customException;

public class SpecialtyNotFoundException extends RuntimeException {
    public SpecialtyNotFoundException(String specialty) {
        super("존재하지 않는 전문 분야: " + specialty);
    }
}
