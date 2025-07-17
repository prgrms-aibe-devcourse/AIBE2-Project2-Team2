package org.example.backend.matching.service;

import org.example.backend.matching.dto.MatchingRequestDto;
import org.example.backend.matching.dto.MatchingResponseDto;
import org.example.backend.matching.dto.MatchingStatusUpdateDto;

public interface MatchingService {

    /**
     * 매칭 요청을 생성합니다.
     */
    MatchingResponseDto createMatching(MatchingRequestDto requestDto);

    /**
     * 매칭 상태를 변경합니다.
     */
    MatchingResponseDto updateMatchingStatus(Long matchingId, MatchingStatusUpdateDto statusDto);

    /**
     * 전문가가 작업을 시작합니다.
     */
    MatchingResponseDto startWork(Long matchingId);

    /**
     * 전문가가 작업을 완료합니다.
     */
    MatchingResponseDto completeWork(Long matchingId);

    /**
     * 클라이언트가 작업 완료를 수락합니다.
     */
    MatchingResponseDto confirmCompletion(Long matchingId);
}
