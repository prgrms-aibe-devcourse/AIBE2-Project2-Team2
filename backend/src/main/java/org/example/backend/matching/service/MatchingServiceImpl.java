package org.example.backend.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.constant.MatchingStatus;
import org.example.backend.entity.Content;
import org.example.backend.entity.Matching;
import org.example.backend.entity.Member;
import org.example.backend.matching.dto.MatchingRequestDto;
import org.example.backend.matching.dto.MatchingResponseDto;
import org.example.backend.matching.dto.MatchingStatusUpdateDto;
import org.example.backend.repository.ContentRepository;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingRepository matchingRepository;
    private final MemberRepository memberRepository;
    private final ContentRepository contentRepository;

    /**
     * 매칭 요청을 생성합니다.
     *
     * @param requestDto 매칭 요청 DTO
     * @return 생성된 매칭 응답 DTO
     */
    @Override
    @Transactional
    public MatchingResponseDto createMatching(MatchingRequestDto requestDto) {
        Member client = memberRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Content content = contentRepository.findById(requestDto.getContentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘텐츠입니다."));

        boolean exists = matchingRepository.existsByMemberAndContent(client, content);
        if (exists) {
            throw new IllegalStateException("이미 해당 콘텐츠에 매칭 요청을 보냈습니다.");
        }

        Matching matching = new Matching(
                client,
                content,
                requestDto.getEstimateUrl(),
                MatchingStatus.REQUESTED
        );

        Matching saved = matchingRepository.save(matching);

        return MatchingResponseDto.builder()
                .matchingId(saved.getMatchingId())
                .contentId(saved.getContent().getContentId())
                .clientId(saved.getMember().getMemberId())
                .status(saved.getStatus())
                .estimateUrl(saved.getEstimateUrl())
                .build();
    }

    /**
     * 매칭 상태를 변경합니다. (ACCEPTED 또는 REJECTED)
     *
     * @param matchingId 매칭 ID
     * @param statusDto 상태 변경 요청 DTO
     * @return 변경된 매칭 응답 DTO
     */
    @Override
    @Transactional
    public MatchingResponseDto updateMatchingStatus(Long matchingId, MatchingStatusUpdateDto statusDto) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭이 존재하지 않습니다."));

        MatchingStatus currentStatus = matching.getStatus();
        MatchingStatus newStatus = statusDto.getStatus();

        if (currentStatus != MatchingStatus.REQUESTED) {
            throw new IllegalStateException("요청 상태일 때만 변경할 수 있습니다.");
        }

        if (newStatus != MatchingStatus.ACCEPTED && newStatus != MatchingStatus.REJECTED) {
            throw new IllegalArgumentException("ACCEPTED 또는 REJECTED 상태만 허용됩니다.");
        }

        matching.changeStatus(newStatus);

        return MatchingResponseDto.builder()
                .matchingId(matching.getMatchingId())
                .contentId(matching.getContent().getContentId())
                .clientId(matching.getMember().getMemberId())
                .status(matching.getStatus())
                .estimateUrl(matching.getEstimateUrl())
                .build();
    }

    /**
     * 전문가가 작업을 시작합니다. (ACCEPTED → IN_PROGRESS)
     *
     * @param matchingId 매칭 ID
     * @return 상태가 변경된 매칭 응답 DTO
     */
    @Override
    @Transactional
    public MatchingResponseDto startWork(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭이 존재하지 않습니다."));

        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("작업을 시작할 수 없는 상태입니다.");
        }

        matching.changeStatus(MatchingStatus.IN_PROGRESS);

        return MatchingResponseDto.builder()
                .matchingId(matching.getMatchingId())
                .contentId(matching.getContent().getContentId())
                .clientId(matching.getMember().getMemberId())
                .status(matching.getStatus())
                .estimateUrl(matching.getEstimateUrl())
                .build();
    }

    /**
     * 전문가가 작업을 완료합니다. (IN_PROGRESS → WORK_COMPLETED)
     *
     * @param matchingId 매칭 ID
     * @return 상태가 변경된 매칭 응답 DTO
     */
    @Override
    @Transactional
    public MatchingResponseDto completeWork(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭이 존재하지 않습니다."));

        if (matching.getStatus() != MatchingStatus.IN_PROGRESS) {
            throw new IllegalStateException("작업 완료 처리는 IN_PROGRESS 상태에서만 가능합니다.");
        }

        matching.changeStatus(MatchingStatus.WORK_COMPLETED);

        return MatchingResponseDto.builder()
                .matchingId(matching.getMatchingId())
                .contentId(matching.getContent().getContentId())
                .clientId(matching.getMember().getMemberId())
                .status(matching.getStatus())
                .estimateUrl(matching.getEstimateUrl())
                .build();
    }

    /**
     * 클라이언트가 작업 완료를 수락합니다. (WORK_COMPLETED → CONFIRMED)
     *
     * @param matchingId 매칭 ID
     * @return 상태가 변경된 매칭 응답 DTO
     */
    @Override
    @Transactional
    public MatchingResponseDto confirmCompletion(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭이 존재하지 않습니다."));

        if (matching.getStatus() != MatchingStatus.WORK_COMPLETED) {
            throw new IllegalStateException("CONFIRMED 처리는 WORK_COMPLETED 상태에서만 가능합니다.");
        }

        matching.changeStatus(MatchingStatus.CONFIRMED);

        return MatchingResponseDto.builder()
                .matchingId(matching.getMatchingId())
                .contentId(matching.getContent().getContentId())
                .clientId(matching.getMember().getMemberId())
                .status(matching.getStatus())
                .estimateUrl(matching.getEstimateUrl())
                .build();
    }

}
