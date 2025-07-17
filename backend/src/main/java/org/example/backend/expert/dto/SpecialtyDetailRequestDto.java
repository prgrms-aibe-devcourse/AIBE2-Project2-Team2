package org.example.backend.expert.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "전문 분야 및 상세 분야 DTO")
public class SpecialtyDetailRequestDto {

    @NotBlank(message = "전문 분야는 비어 있을 수 없습니다.")
    @Schema(description = "전문 분야 이름", example = "디자인")
    private String specialty;

    @NotEmpty(message = "상세 분야는 최소 1개 이상 선택해야 합니다.")
    @Size(min = 1, max = 5, message = "상세 분야는 1~5개 선택해야 합니다.")
    @Schema(description = "상세 분야 목록", example = "[\"UI 디자인\", \"UX 설계\"]")
    private List<@NotBlank String> detailFields;
}
