package org.example.backend.constant;

public enum MatchingStatus {
    WAITING_PAYMENT,          // 결제 대기 중 (매칭 시작단계)
    ACCEPTED,           // 결제 후, 자동 수락
    IN_PROGRESS,        // 작업 진행 중 (전문가가 작업 시작)
    WORK_COMPLETED,     // 전문가가 작업 완료
    CONFIRMED,          // 의뢰자가 완료 수락 → 매칭 완료
    REJECTED,            // 매칭 거절
    CANCELLED           // 의뢰자가 매칭 취소
}
