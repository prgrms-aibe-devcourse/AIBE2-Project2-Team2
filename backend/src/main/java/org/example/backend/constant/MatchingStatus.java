package org.example.backend.constant;

public enum MatchingStatus {
    COUNSEL,          // 상담 신청 << 이거 빼자 애매하다
    REQUESTED,		// 매칭 신청 + 초기 견적서(질문답변완료 및 저장) + 전문가에게 이메일전송 + 채팅 시작 + 견적서 수정사항 등을 소통
    PAID, 			//  의뢰자가 전문가를 통해서 견적 옵션 수정 한 후에, 해당 견적서 금액으로 결제
    ACCEPTED,           // 결제 후, 전문가가 견적서를 확인한 후에 수락을 누른 상
    IN_PROGRESS,        // 작업 진행 중 (전문가가 작업 시작)
    WORK_COMPLETED,     // 전문가가 작업 완료
    CONFIRMED,          // 의뢰자가 완료 수락 → 매칭 완료
    REJECTED            // 매칭 거절
}
