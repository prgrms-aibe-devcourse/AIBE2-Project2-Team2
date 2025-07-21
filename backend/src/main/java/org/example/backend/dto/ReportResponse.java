package org.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.backend.constant.ReportStatus;

@Getter
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private Long reportedId;
    private String reportedNickname;
    private String reason;
    private ReportStatus status;
}
