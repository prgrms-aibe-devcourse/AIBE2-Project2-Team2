package org.example.backend.exception.customException;

public class InvalidThumbnailIndexException extends RuntimeException {
    public InvalidThumbnailIndexException(String message) {
        super(message);
    }
}
