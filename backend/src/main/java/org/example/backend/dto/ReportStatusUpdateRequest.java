package org.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportStatusUpdateRequest {

    @Schema(example = "IN_PROGRESS", description = "변경할 신고 상태 (SUBMITTED, IN_PROGRESS, COMPLETED, CANCELED)")
    private String status;
}
