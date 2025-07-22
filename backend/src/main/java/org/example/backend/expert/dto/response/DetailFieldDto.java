package org.example.backend.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DetailFieldDto {
    private String specialty;   // 상위 전문 분야 이름
    private List<String> detailFields;
}
