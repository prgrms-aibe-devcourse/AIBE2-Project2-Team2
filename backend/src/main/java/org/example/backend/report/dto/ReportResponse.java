package org.example.backend.report.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.constant.ReportStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private Long reportedId;
    private String reportedNickname;
    private String reason;
    private String category;
    private LocalDateTime createdAt;
    private ReportStatus status;
    private String resolverNickname;
    private String resolverComment;
    private LocalDateTime resolvedAt;
}
