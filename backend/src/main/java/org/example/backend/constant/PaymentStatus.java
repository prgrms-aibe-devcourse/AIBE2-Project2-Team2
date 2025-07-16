package org.example.backend.constant;

public enum PaymentStatus {
    NOT_PAID,  // 결제 전
    PAID,       // 결제 완료
    FAILED,      // 결제 실패
    CANCELLED,   // 결제 취소
    REFUNDED   // 환불 완료
}
