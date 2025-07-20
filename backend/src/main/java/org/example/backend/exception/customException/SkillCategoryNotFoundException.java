package org.example.backend.exception.customException;

public class SkillCategoryNotFoundException extends RuntimeException{
    public SkillCategoryNotFoundException(String message) {
        super(message);
    }
}
