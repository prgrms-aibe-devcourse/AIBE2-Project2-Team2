package org.example.backend.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "기술 스킬 DTO")
public class SkillDto {

    @NotBlank
    @Schema(description = "기술 카테고리 이름", example = "IT/프로그래밍")
    private String category;

    @NotBlank
    @Schema(description = "기술 이름", example = "Java")
    private String name;
}