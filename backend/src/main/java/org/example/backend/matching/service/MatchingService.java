package org.example.backend.matching.service;

import org.example.backend.constant.PaymentStatus;
import org.example.backend.matching.dto.MatchingRequestDto;
import org.example.backend.matching.dto.MatchingResponseDto;
import org.example.backend.matching.dto.MatchingStatusUpdateDto;

public interface MatchingService {

    /**
     * 매칭 요청 생성 (→ WAITING_PAYMENT)
     */
    MatchingResponseDto createMatching(MatchingRequestDto requestDto);

    /**
     * 매칭 상태 변경
     */
    MatchingResponseDto updateMatchingStatus(Long matchingId, MatchingStatusUpdateDto statusDto);

    /**
     * 전문가가 작업 시작 (ACCEPTED → IN_PROGRESS)
     */
    MatchingResponseDto startWork(Long matchingId);

    /**
     * 전문가가 작업 완료 (IN_PROGRESS → WORK_COMPLETED)
     */
    MatchingResponseDto completeWork(Long matchingId);

    /**
     * 클라이언트가 작업 승인 (WORK_COMPLETED → CONFIRMED)
     */
    MatchingResponseDto confirmCompletion(Long matchingId);

    /**
     * 결제 상태에 따라 매칭 상태 자동 전이
     */
    MatchingResponseDto updateMatchingStatusByPaymentResult(Long matchingId, PaymentStatus paymentStatus);

    /**
     * 매칭 상세 정보 조회
     */
    MatchingResponseDto getMatchingDetail(Long matchingId);
}
