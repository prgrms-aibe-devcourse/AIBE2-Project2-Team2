package org.example.backend.repository;

import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchingRepositoryCustom {
    List<MatchingSummaryDto> findExpertMatchingSummaries(String expertEmail, MatchingSearchCondition condition, Pageable pageable);
}
