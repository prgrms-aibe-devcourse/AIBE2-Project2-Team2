package org.example.backend.exception.customException;


public class DetailFieldNotFoundException extends RuntimeException {
    public DetailFieldNotFoundException(String detailFieldName) {
        super("존재하지 않는 상세 분야: " + detailFieldName);
    }
}
