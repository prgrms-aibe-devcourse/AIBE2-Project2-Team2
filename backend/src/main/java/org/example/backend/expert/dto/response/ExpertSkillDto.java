package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpertSkillDto {
    // 기술 카테고리 이름
    private String category;
    private String name;

    @QueryProjection
    public ExpertSkillDto(String  category, String name) {
        this.category = category;
        this.name = name;
    }
}
