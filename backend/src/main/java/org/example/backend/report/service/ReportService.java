package org.example.backend.report.service;


import org.example.backend.report.dto.ReportResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ReportService {
    void submitReport(Long reporterId, Long reportedId, String reason);

    List<ReportResponse> getReportsByStatus(String status);

    void updateReportStatus(Long reportId, String newStatus,String email);

    void updateStatusAndComment(Long reportId, String status, String resolverComment,String email); // ✅ 추가

    void deleteReport(Long reportId);

    ReportResponse getReportById(Long id);

    List<ReportResponse> getReportsByCurrentUser(String email);

    void submitReportByNickname(String reporterEmail, String reportedNickname, String reason);

}
