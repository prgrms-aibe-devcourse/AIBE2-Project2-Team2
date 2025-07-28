package org.example.backend.repository;

import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryExpertDto;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchingRepositoryCustom {
    // 전문가가 일반유저와의 매칭 내역을 조회할 때
    Page<MatchingSummaryUserDto> findExpertMatchingSummaries(String expertEmail, MatchingSearchCondition condition, Pageable pageable);

    // 일반유저가 전문가와의 매칭 내역을 조회할 때
    Page<MatchingSummaryExpertDto> findUserMatchingSummaries(String userEmail, MatchingSearchCondition condition, Pageable pageable);
}
