package org.example.backend.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SkillCategoryDto {
    private String categoryName;       // 상위 카테고리 이름
    private List<String> skills;       // 해당 카테고리에 속한 기술들
}
