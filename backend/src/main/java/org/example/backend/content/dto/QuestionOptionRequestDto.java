package org.example.backend.content.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class QuestionOptionRequestDto {
    @NotNull
    private String optionText;

    @NotNull
    @Min(0)
    private Long additionalPrice;

    @NotNull
    private Long questionId;
}
