package org.example.backend.content.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionRequestDto {
    private String questionText;
    private boolean isMultipleChoice;
    private List<QuestionOptionRequestDto> options;
}
