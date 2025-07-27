package org.example.backend.Estimate.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class EstimateRequestDto {
    // 유저가 선택한 옵션들의 ID 목록
    private List<Long> selectedOptionIds;
}

