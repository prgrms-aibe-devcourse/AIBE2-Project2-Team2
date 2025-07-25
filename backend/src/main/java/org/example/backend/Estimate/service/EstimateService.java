package org.example.backend.Estimate.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.Estimate.dto.EstimateRecordResponseDto;
import org.example.backend.entity.*;
import org.example.backend.repository.EstimateRecordRepository;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.QuestionOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstimateService {
    private final MatchingRepository matchingRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final EstimateRecordRepository estimateRecordRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public EstimateRecordResponseDto createEstimate(Long matchingId, List<Long> selectedOptionIds, String email) {
        // 1. 매칭 정보 조회 (존재하지 않으면 예외)
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new RuntimeException("매칭을 찾을 수 없습니다."));

        // 2. 권한 체크: 로그인한 사용자가 해당 매칭의 주인인지 확인
        if (!matching.getMember().getEmail().equals(email)) {
            throw new AccessDeniedException("본인만 견적서를 제출할 수 있습니다.");
        }

        // 3. 해당 매칭이 연결된 콘텐츠 정보 가져오기
        Content content = matching.getContent();

        // 4. 선택된 옵션 ID 목록으로 실제 옵션 엔티티들 조회
        List<QuestionOption> options = questionOptionRepository.findAllById(selectedOptionIds);

        // 5. 옵션들의 추가 금액 합계 계산
        Long optionsTotal = options.stream().mapToLong(QuestionOption::getAdditionalPrice).sum();

        // 6. 총 견적 금액 = 콘텐츠 기본 예산 + 옵션 추가금액 합
        Long totalPrice = content.getBudget() + optionsTotal;

        // 7. EstimateRecord(견적 스냅샷) 엔티티 생성 및 정보 입력
        EstimateRecord record = new EstimateRecord();
        record.setMatching(matching);
        record.setTotalPrice(totalPrice);

        // 8. 선택된 옵션들을 SelectedProduct(옵션 스냅샷) 엔티티로 변환 및 정보 복사
        List<SelectedProduct> selectedProducts = options.stream()
                .map(opt -> {
                    SelectedProduct p = new SelectedProduct();
                    p.setName(opt.getOptionText());        // 옵션명 스냅샷
                    p.setPrice(opt.getAdditionalPrice());  // 옵션 추가 금액 스냅샷
                    p.setEstimateRecord(record);           // EstimateRecord와 연관관계 설정
                    return p;
                })
                .collect(Collectors.toList());

        // 9. EstimateRecord에 선택된 옵션(SelectedProduct) 리스트 연결
        record.getSelectedProducts().addAll(selectedProducts);

        // 10. 견적서 및 선택 옵션들 DB에 저장(cascade가 설정되어 있으면 옵션들도 같이 저장됨)
        estimateRecordRepository.save(record);

        // 11. 응답 DTO용으로 선택 옵션 정보 매핑
        List<EstimateRecordResponseDto.SelectedOptionDto> selectedOptionDtos = selectedProducts.stream()
                .map(p -> EstimateRecordResponseDto.SelectedOptionDto.builder()
                        .name(p.getName())
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());

        // 12. 최종적으로 EstimateRecordResponseDto로 변환해서 응답
        return EstimateRecordResponseDto.builder()
                .estimateRecordId(record.getEstimateRecordId())
                .totalPrice(record.getTotalPrice())
                .selectedOptions(selectedOptionDtos)
                .build();
    }
}
