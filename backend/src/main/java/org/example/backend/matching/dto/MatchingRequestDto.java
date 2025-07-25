package org.example.backend.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingRequestDto {

    @NotNull(message = "회원 ID는 필수입니다.")
    private Long memberId;

    @NotNull(message = "콘텐츠 ID는 필수입니다.")
    private Long contentId;

    @NotEmpty(message = "견적 항목은 하나 이상 선택해야 합니다.")
    private List<EstimateItemDto> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimateItemDto {
        private String name;
        private Long price;
    }
}