package org.example.backend.expert.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpertSignupMetaDto {
    private List<DetailFieldDto> detailFields;
    private List<SkillCategoryDto> skills;
    private List<String> regions; // 이건 임시로 static list 줄 수도 있음
}
