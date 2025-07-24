package org.example.backend.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.MatchingStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResponseDto {

    private Long matchingId;
    private Long memberId;
    private Long contentId;
    private MatchingStatus status;
    private String rejectedReason;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalPrice;
    private List<EstimateItem> items;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EstimateItem {
        private String name;
        private Long price;
    }
}
