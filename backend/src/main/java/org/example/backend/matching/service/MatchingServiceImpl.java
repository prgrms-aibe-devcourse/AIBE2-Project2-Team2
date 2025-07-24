package org.example.backend.matching.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.constant.MatchingStatus;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.entity.*;
import org.example.backend.matching.dto.MatchingRequestDto;
import org.example.backend.matching.dto.MatchingResponseDto;
import org.example.backend.matching.dto.MatchingStatusUpdateDto;
import org.example.backend.repository.ContentRepository;
import org.example.backend.repository.EstimateRecordRepository;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.MemberRepository;
// [이메일 알림 서비스 import]
import org.example.backend.notification.service.MailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingRepository matchingRepository;
    private final MemberRepository memberRepository;
    private final ContentRepository contentRepository;
    private final EstimateRecordRepository estimateRecordRepository;

    // [이메일 알림 서비스 주입]
    private final MailService mailService;

    /**
     * 매칭 요청을 생성합니다.
     * 클라이언트가 콘텐츠에 대해 견적 항목들을 선택하고 요청 시,
     * 매칭은 WAITING_PAYMENT 상태로 생성되며 견적 정보와 선택 상품도 저장됩니다.
     */
    @Override
    @Transactional
    public MatchingResponseDto createMatching(MatchingRequestDto requestDto) {
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new NoSuchElementException("회원을 찾을 수 없습니다."));
        Content content = contentRepository.findById(requestDto.getContentId())
                .orElseThrow(() -> new NoSuchElementException("콘텐츠를 찾을 수 없습니다."));

        Matching matching = Matching.builder()
                .member(member)
                .content(content)
                .status(MatchingStatus.WAITING_PAYMENT)
                .build();

        Matching saved = matchingRepository.save(matching);

        long totalPrice = requestDto.getItems().stream()
                .mapToLong(MatchingRequestDto.EstimateItemDto::getPrice)
                .sum();

        // EstimateRecord + SelectedProduct 생성
        EstimateRecord estimate = EstimateRecord.builder()
                .matching(saved)
                .totalPrice(totalPrice)
                .build();

        // 견적 상품 저장
        List<SelectedProduct> products = requestDto.getItems().stream()
                .map(item -> SelectedProduct.builder()
                        .estimateRecord(estimate)
                        .name(item.getName())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
        estimate.getSelectedProducts().addAll(products);

        estimateRecordRepository.save(estimate);

        return buildMatchingResponse(saved, estimate);
    }

    /**
     * 매칭 상태를 변경합니다.
     */
    @Override
    @Transactional
    public MatchingResponseDto updateMatchingStatus(Long matchingId, MatchingStatusUpdateDto statusDto) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        MatchingStatus newStatus = statusDto.getStatus();
        String reason = statusDto.getReason();

        switch (newStatus) {
            case CANCELLED:
                matching.cancel();
                break;
            case REJECTED:
                matching.rejectByExpert(reason);
                break;
            default:
                matching.changeStatus(newStatus);
        }

        // [이메일 알림 추가]
        if (newStatus == MatchingStatus.ACCEPTED) {
            // 전문가에게 견적 요청 알림
            Member expert = matching.getContent().getMember();
            String expertEmail = expert.getEmail();
            mailService.sendSimpleMail(
                    expertEmail,
                    "견적 요청 알림",
                    "견적 요청이 왔습니다."
            );
        } else if (newStatus == MatchingStatus.WORK_COMPLETED) {
            // 의뢰자에게 작업 완료 알림
            Member client = matching.getMember();
            String clientEmail = client.getEmail();
            mailService.sendSimpleMail(
                    clientEmail,
                    "작업 완료 알림",
                    "작업이 완료되었습니다."
            );
        }

        EstimateRecord estimate = matching.getEstimateRecord();

        return buildMatchingResponse(matching, estimate);
    }

    /**
     * 전문가가 작업을 시작합니다. (ACCEPTED → IN_PROGRESS)
     */
    @Override
    @Transactional
    public MatchingResponseDto startWork(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        if (matching.getStatus() != MatchingStatus.ACCEPTED) {
            throw new IllegalStateException("작업은 ACCEPTED 상태에서만 시작할 수 있습니다.");
        }

        matching.startWork();
        return buildMatchingResponse(matching, matching.getEstimateRecord());
    }

    /**
     * 전문가가 작업을 완료합니다. (IN_PROGRESS → WORK_COMPLETED)
     */
    @Override
    @Transactional
    public MatchingResponseDto completeWork(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        if (matching.getStatus() != MatchingStatus.IN_PROGRESS) {
            throw new IllegalStateException("작업 완료는 IN_PROGRESS 상태에서만 가능합니다.");
        }

        matching.completeWork();
        return buildMatchingResponse(matching, matching.getEstimateRecord());
    }

    /**
     * 클라이언트가 작업 완료를 승인합니다. (WORK_COMPLETED → CONFIRMED)
     */
    @Override
    @Transactional
    public MatchingResponseDto confirmCompletion(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        if (matching.getStatus() != MatchingStatus.WORK_COMPLETED) {
            throw new IllegalStateException("작업 승인 처리는 WORK_COMPLETED 상태에서만 가능합니다.");
        }

        matching.confirmCompletion();
        return buildMatchingResponse(matching, matching.getEstimateRecord());
    }

    /**
     * 결제 결과에 따라 매칭 상태를 자동으로 변경합니다.
     * 결제 성공 시 PAID → ACCEPTED, 실패 시 FAILED or CANCELLED → CANCELLED.
     */
    @Override
    @Transactional
    public MatchingResponseDto updateMatchingStatusByPaymentResult(Long matchingId, PaymentStatus paymentStatus) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        switch (paymentStatus) {
            case PAID:
                matching.accept();
                break;
            case FAILED:
            case CANCELLED:
                matching.cancel();
                break;
            default:
                throw new IllegalArgumentException("처리할 수 없는 결제 상태입니다: " + paymentStatus);
        }

        return buildMatchingResponse(matching, matching.getEstimateRecord());
    }

    /**
     * 매칭 상세 정보를 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public MatchingResponseDto getMatchingDetail(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new NoSuchElementException("매칭을 찾을 수 없습니다."));

        EstimateRecord estimate = matching.getEstimateRecord();

        return buildMatchingResponse(matching, estimate);
    }

    /**
     * 공통 응답 DTO를 생성합니다.
     * EstimateRecord와 SelectedProduct까지 DTO로 매핑.
     */
    private MatchingResponseDto buildMatchingResponse(Matching matching, EstimateRecord estimate) {
        List<MatchingResponseDto.EstimateItem> items = null;
        if (estimate != null && estimate.getSelectedProducts() != null) {
            items = estimate.getSelectedProducts().stream()
                    .map(product -> MatchingResponseDto.EstimateItem.builder()
                            .name(product.getName())
                            .price(product.getPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return MatchingResponseDto.builder()
                .matchingId(matching.getMatchingId())
                .memberId(matching.getMember().getMemberId())
                .contentId(matching.getContent().getContentId())
                .status(matching.getStatus())
                .startDate(matching.getStartDate())
                .endDate(matching.getEndDate())
                .rejectedReason(matching.getRejectedReason())
                .totalPrice(estimate != null ? estimate.getTotalPrice() : null)
                .items(items)
                .build();
    }
}
