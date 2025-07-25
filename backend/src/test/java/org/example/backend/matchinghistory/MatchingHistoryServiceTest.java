package org.example.backend.matchinghistory;

import org.example.backend.constant.JoinType;
import org.example.backend.constant.MatchingStatus;
import org.example.backend.constant.Status;
import org.example.backend.entity.Content;
import org.example.backend.entity.Matching;
import org.example.backend.entity.Member;
import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
import org.example.backend.repository.ContentRepository;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MatchingHistoryServiceTest {

    @Autowired
    private MatchingHistoryService matchingHistoryService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @Test
    @DisplayName("전문가 매칭 이력 요약 조회 - 정상 동작")
    void getExpertMatchingHistoriesTest() {
        // 전문가 등록
        Member expert = Member.createExpert(
                "expert@example.com",
                "encodedPwd",
                "전문가",
                "01012345678",
                JoinType.NORMAL
        );
        memberRepository.save(expert);

        // 유저 등록
        Member user = Member.create(
                "user@example.com",
                "encodedPwd",
                "유저",
                "01000000000",
                JoinType.NORMAL
        );
        memberRepository.save(user);

        // 콘텐츠 등록
        Content content = new Content(
                expert,
                "로고 디자인",
                "고퀄리티 로고 디자인 제작",
                500000L,
                Status.ACTIVE,
                "디자인"
        );
        contentRepository.save(content);

        // 매칭 등록
        Matching matching = new Matching(
                user,  // 신청자(user)
                content,
                MatchingStatus.ACCEPTED,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 15)
        );
        matchingRepository.save(matching);

        // 검색 조건 및 페이지 요청
        MatchingSearchCondition condition = new MatchingSearchCondition();
        PageRequest pageable = PageRequest.of(0, 10);

        // 실행
        Page<MatchingSummaryUserDto> result = matchingHistoryService.getExpertMatchingHistories(
                expert.getEmail(), condition, pageable
        );

        // 검증
        assertThat(result.getTotalElements()).isEqualTo(1);
        MatchingSummaryUserDto dto = result.getContent().get(0);
        assertThat(dto.getContentTitle()).isEqualTo("로고 디자인");
        assertThat(dto.getMatchingStatus()).isEqualTo(MatchingStatus.ACCEPTED);
        assertThat(dto.getWorkStartDate()).isEqualTo(LocalDate.of(2025, 7, 1));
        assertThat(dto.getWorkEndDate()).isEqualTo(LocalDate.of(2025, 7, 15));
    }

}