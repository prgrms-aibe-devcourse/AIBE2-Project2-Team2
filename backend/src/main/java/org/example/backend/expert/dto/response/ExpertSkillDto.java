package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpertSkillDto {
    // 기술 카테고리 이름
    private String skillCategoryName;
    // 기술 이름
    private String skillName;

    @QueryProjection
    public ExpertSkillDto(String skillCategoryName, String skillName) {
        this.skillCategoryName = skillCategoryName;
        this.skillName = skillName;
    }
}
