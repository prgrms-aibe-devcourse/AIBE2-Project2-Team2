package org.example.backend.Estimate.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EstimateRecordResponseDto {
    // 견적서(스냅샷) ID
    private Long estimateRecordId;
    // 총 견적 금액 (기본금액 + 옵션 추가금액 합계)
    private Long totalPrice;
    // 견적에 포함된 옵션(스냅샷) 목록
    private List<SelectedOptionDto> selectedOptions;

    @Getter
    @Builder
    public static class SelectedOptionDto {
        // 옵션명(견적 생성 시점에 복사된 값)
        private String name;
        // 옵션 추가 금액(견적 생성 시점에 복사된 값)
        private Long price;
    }
}
