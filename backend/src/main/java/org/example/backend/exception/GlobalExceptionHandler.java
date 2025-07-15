package org.example.backend.exception;


import org.example.backend.exception.customException.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            String errorCode,
            String message,
            Map<String, String> errors
    ) {
        request.setAttribute("businessErrorCode", errorCode);
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "errorCode", errorCode,
                "message", message,
                "errors", errors
        );
        return ResponseEntity.status(status).body(body);
    }

    // Validation 오류 핸들링
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "잘못된 입력입니다.",
                        (existing, replacement) -> existing
                ));

        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "입력값이 올바르지 않습니다.",
                fieldErrors
        );
    }

    // UserNotFoundException 핸들링
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    // 파일 업로드 시 파일 문제 핸들링
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFile(InvalidFileException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_FILE",
                ex.getMessage(),
                null
        );
    }

    // 파이어 베이스에 파일 업로드 실패 핸들링
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handleImageUploadError(ImageUploadException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "IMAGE_UPLOAD_ERROR",
                ex.getMessage(),
                null
        );
    }

    // 파이어 베이스에 파일 이미지 삭제 실패 핸들링
    @ExceptionHandler(ImageDeleteException.class)
    public ResponseEntity<Map<String, Object>> handleImageDeleteError(ImageDeleteException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "IMAGE_DELETE_ERROR",
                ex.getMessage(),
                null
        );
    }

}
