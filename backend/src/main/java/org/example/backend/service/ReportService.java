package org.example.backend.service;

import org.example.backend.dto.ReportResponse;
import java.util.List;

public interface ReportService {
    void submitReport(Long reporterId, Long reportedId, String reason);

    List<ReportResponse> getReportsByStatus(String status);

    void updateReportStatus(Long reportId, String newStatus);

}
