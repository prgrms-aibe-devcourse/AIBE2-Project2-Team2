package org.example.backend.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportUpdateRequest {
    private String status;
    private String resolverComment;
}
