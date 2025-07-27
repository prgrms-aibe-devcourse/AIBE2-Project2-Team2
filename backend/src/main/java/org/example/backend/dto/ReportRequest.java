package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    @Schema(description = "신고자 ID", example = "1")
    private Long reporterId;

    @Schema(description = "피신고자 ID", example = "2")
    private Long reportedMemberId;

    @Schema(description = "신고 사유", example = "약속을 지키지 않았어요")
    private String reason;
}
