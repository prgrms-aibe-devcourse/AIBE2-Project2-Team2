package org.example.backend.exception;


import lombok.extern.slf4j.Slf4j;
import org.example.backend.exception.customException.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
                "errors", errors != null ? errors : Map.of()
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
    // 유저 정보 조회시 유저 정보가 없을 때 핸들링
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMemberNotFound(MemberNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "MEMBER_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 유저가 이미 전문가일 때 핸들링
    @ExceptionHandler(AlreadyExpertException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyExpert(AlreadyExpertException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "ALREADY_EXPERT",
                ex.getMessage(),
                null
        );
    }
    // 없는 전문 분야를 요청했을 때 핸들링
    @ExceptionHandler(SpecialtyNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSpecialtyNotFound(SpecialtyNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "SPECIALTY_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 없는 상세 분야를 요청했을 때 핸들링
    @ExceptionHandler(DetailFieldNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDetailFieldNotFound(DetailFieldNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "DETAIL_FIELD_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 일반 예외처리 추가
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("예상치 못한 오류 발생", ex);
        return buildErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다.",
                null
        );
    }
    // 해당 스킬 카테고리가 없을 때 핸들링
    @ExceptionHandler(SkillCategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSkillCategoryNotFound(SkillCategoryNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "SKILL_CATEGORY_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 해당 스킬이 없을 때 핸들링
    @ExceptionHandler(SkillNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSkillNotFound(SkillNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "SKILL_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    // 권한이 없는 사용자가 접근했을 때 핸들링
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "접근 권한이 없습니다.",
                null
        );
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReportNotFound(ReportNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "REPORT_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    @ExceptionHandler(InvalidReportStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStatus(InvalidReportStatusException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_REPORT_STATUS",
                ex.getMessage(),
                null
        );
    }

    // 전문가 권한이 없을 때 핸들링
    @ExceptionHandler(NotExpertException.class)
    public ResponseEntity<Map<String, Object>> handleNotExpert(NotExpertException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.FORBIDDEN,
                "NOT_EXPERT",
                ex.getMessage(),
                null
        );
    }
    // 전문가 프로필이 없을 때 핸들링
    @ExceptionHandler(ExpertProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExpertProfileNotFound(ExpertProfileNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "EXPERT_PROFILE_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 해당 포트폴리오가 없을 때 핸들링
    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePortfolioNotFound(PortfolioNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "PORTFOLIO_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 포트폴리오 이미지를 등록/수정 할 때 이미지가 유효하지 않을 때 핸들링
    @ExceptionHandler(InvalidPortfolioImageException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPortfolioImage(InvalidPortfolioImageException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_PORTFOLIO_IMAGE",
                ex.getMessage(),
                null
        );
    }
    // 썸네일 이미지 인덱스가 잘못되었을 때 핸들링
    @ExceptionHandler(InvalidThumbnailIndexException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidThumbnailIndex(InvalidThumbnailIndexException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_THUMBNAIL_INDEX",
                ex.getMessage(),
                null
        );
    }
    // 결제 정보가 없는 경우 핸들링
    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentInfoNotFound(PaymentNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "PAYMENT_INFO_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 견적 정보가 없는 경우 핸들링
    @ExceptionHandler(EstimateRecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEstimateNotFound(EstimateRecordNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "ESTIMATE_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }
    // 결제 상태가 잘못된 경우 핸들링
    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPaymentStatus(InvalidPaymentStatusException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_PAYMENT_STATUS",
                ex.getMessage(),
                null
        );
    }
    // 매칭 상태가 잘못된 경우 핸들링
    @ExceptionHandler(InvalidMatchingStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidMatchingStatus(InvalidMatchingStatusException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.BAD_REQUEST,
                "INVALID_MATCHING_STATUS",
                ex.getMessage(),
                null
        );
    }

    // 콘텐츠를 찾을 수 없을 때 핸들링
    @ExceptionHandler(ContentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleContentNotFound(ContentNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.NOT_FOUND,
                "CONTENT_NOT_FOUND",
                ex.getMessage(),
                null
        );
    }

    // 콘텐츠 접근 권한이 없을 때 핸들링
    @ExceptionHandler(NoContentPermissionException.class)
    public ResponseEntity<Map<String, Object>> handleNoContentPermission(NoContentPermissionException ex, HttpServletRequest request) {
        return buildErrorResponse(
                request,
                HttpStatus.FORBIDDEN,
                "NO_CONTENT_PERMISSION",
                ex.getMessage(),
                null
        );
    }
}