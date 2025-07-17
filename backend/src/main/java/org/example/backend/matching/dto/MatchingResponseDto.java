package org.example.backend.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.constant.MatchingStatus;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class MatchingResponseDto {

    private Long matchingId;
    private Long contentId;
    private Long clientId;
    private MatchingStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private String estimateUrl;
    private String message;
}
