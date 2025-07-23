package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpertFieldDto {
    // 전문 분야 이름
    private String specialtyName;
    // 상세 분야 이름
    private String detailFieldName;


    @QueryProjection
    public ExpertFieldDto(String specialtyName, String detailFieldName) {
        this.specialtyName = specialtyName;
        this.detailFieldName = detailFieldName;
    }
}
