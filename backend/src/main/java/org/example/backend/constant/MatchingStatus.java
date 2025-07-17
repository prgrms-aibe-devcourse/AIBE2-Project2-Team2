package org.example.backend.constant;

public enum MatchingStatus {
    COUNSEL,          // 상담 신청 (의뢰자가 상담 신청)
    REQUESTED,          // 매칭신청
    ACCEPTED,           // 전문가가 매칭 수락
    PAID,               // 의뢰자의 결제 완료 >> 전문가에게 연락 채팅창 + 카카오 + 이메일 결제 완료 안내
    IN_PROGRESS,        // 작업 진행 중 (전문가가 작업 시작)
    WORK_COMPLETED,     // 전문가가 작업 완료
    CONFIRMED,          // 의뢰자가 완료 수락 → 매칭 완료
    REJECTED            // 매칭 거절
}
