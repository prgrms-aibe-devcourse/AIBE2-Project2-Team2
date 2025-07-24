package org.example.backend.content.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentRequestDto {
    @Schema(example = "고퀄리티 로고 디자인 제작")
    private String title;

    @Schema(example = "전문 디자이너가 직접 제작하는 맞춤형 로고 디자인! 빠르고 퀄리티 높게 작업합니다.")
    private String description;

    @Schema(example = "120000")
    private Long budget;

    @Schema(description = "카테고리 ID", example = "1", required = true)
    private Long categoryId;

    @Schema(
      description = "질문 리스트",
      example = "[{'questionText':'로고 스타일을 선택해주세요.','multipleChoice':false,'options':[{'optionText':'심플','additionalPrice':0},{'optionText':'프리미엄','additionalPrice':20000}]},{'questionText':'원하는 파일 포맷을 선택하세요.','multipleChoice':true,'options':[{'optionText':'JPG','additionalPrice':0},{'optionText':'PNG','additionalPrice':0},{'optionText':'AI','additionalPrice':10000}]}]"
    )
    private List<QuestionDto> questions;
}