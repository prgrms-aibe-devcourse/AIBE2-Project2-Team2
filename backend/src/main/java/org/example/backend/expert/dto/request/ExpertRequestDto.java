package org.example.backend.expert.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "ExpertRequestDto", description = "전문가 프로필 등록 요청 DTO")
public class ExpertRequestDto {

    @NotEmpty(message = "전문 분야는 최소 1개 이상 선택해야 합니다.")
    @Size(min = 1, max = 3, message = "전문 분야는 1~3개 선택해야 합니다.")
    @Valid
    private List<SpecialtyDetailRequestDto> specialties;

    @NotBlank
    @Schema(description = "자기소개", example = "안녕하세요, 디자인 전문가입니다.")
    private String introduction;

    @NotBlank
    @Schema(description = "활동 지역", example = "서울특별시 강남구")
    private String region;

    @NotNull
    @Min(0)
    @Schema(description = "총 경력 연수", example = "5")
    private Integer totalCareerYears;

    @NotBlank
    @Schema(description = "학력", example = "서울대학교 디자인학과")
    private String education;

    @NotNull
    @Min(0)
    @Schema(description = "직원 수", example = "10")
    private Integer employeeCount;

    @Schema(description = "웹사이트 URL", example = "https://example.com")
    private String websiteUrl;

    @Schema(description = "페이스북 URL", example = "https://facebook.com/username")
    private String facebookUrl;

    @Schema(name = "xUrl", description = "X(구 트위터) URL", example = "https://x.com/username")
    @JsonProperty("xUrl")
    private String xUrl;

    @Schema(description = "인스타그램 URL", example = "https://instagram.com/username")
    private String instagramUrl;

    @NotEmpty(message = "기술은 최소 1개 이상 선택해야 합니다.")
    @Size(min = 1, max = 20, message = "기술은 1~20개 선택해야 합니다.")
    @Schema(description = "기술 목록")
    private List<@Valid SkillDto> skills;

    @NotEmpty
    @Schema(description = "경력 설명 목록", example = "[\"ABC 회사에서 3년 근무\", \"XYZ 디자인 프로젝트 참여\"]")
    private List<@NotBlank String> careers;
}
