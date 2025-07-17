package org.example.backend.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class MatchingRequestDto {

    @NotNull(message = "컨텐츠 ID는 필수입니다.")
    private Long contentId;

    @NotNull(message = "의뢰자 ID는 필수입니다.")
    private Long clientId;

    private String estimateUrl; // 견적서 링크 (선택)

    private String message; // 의뢰 내용 또는 요청 메시지
}
