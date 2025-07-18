package org.example.backend.exception.customException;

import javax.validation.constraints.NotBlank;

public class SkillNotFoundException extends RuntimeException{
    public SkillNotFoundException(String message) {
        super(message);
    }
}
