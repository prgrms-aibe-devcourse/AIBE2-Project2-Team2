package org.example.backend.constant;

public enum MatchingStatus {
    REQUESTED,          // 상담 신청 (의뢰자가 상담 신청)
    PAID,               // 결제 완료 (예약/신청 완료)
    ACCEPTED,           // 전문가가 매칭 수락
    IN_PROGRESS,        // 작업 진행 중 (전문가가 작업 시작)
    WORK_COMPLETED,     // 전문가가 작업 완료
    CONFIRMED,          // 의뢰자가 완료 수락 → 매칭 완료
    REJECTED            // 매칭 거절
}
