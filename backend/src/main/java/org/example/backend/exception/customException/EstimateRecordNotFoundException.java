package org.example.backend.exception.customException;

public class EstimateRecordNotFoundException extends RuntimeException{
    public EstimateRecordNotFoundException(String message) {
        super(message);
    }
}
