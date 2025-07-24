package org.example.backend.content.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    private String questionText;
    @JsonProperty("multipleChoice")
    private boolean isMultipleChoice;
    private List<QuestionOptionDto> options;
}
