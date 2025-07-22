package org.example.backend.matchinghistory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryExpertDto;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
import org.example.backend.repository.MatchingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchingHistoryService {

    private final MatchingRepository matchingRepository;

    public List<MatchingSummaryUserDto> getExpertMatchingHistories(String expertEmail, MatchingSearchCondition condition, Pageable pageable) {
        // condition에서 필터 파싱 후 matchingRepositoryCustom 호출
        return matchingRepository.findExpertMatchingSummaries(expertEmail, condition, pageable);
    }

    public List<MatchingSummaryExpertDto> getUserMatchingHistories(String email, MatchingSearchCondition condition, Pageable pageable) {
        return matchingRepository.findUserMatchingSummaries(email, condition, pageable);
    }
}
