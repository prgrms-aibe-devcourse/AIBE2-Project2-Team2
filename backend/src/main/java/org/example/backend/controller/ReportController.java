package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReportRequest;
import org.example.backend.dto.ReportResponse;
import org.example.backend.dto.ReportStatusUpdateRequest;
import org.example.backend.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 신고 접수 및 등록
    @PostMapping
    public ResponseEntity<Void> reportMember(@RequestBody ReportRequest request) {
        reportService.submitReport(
                request.getReporterId(),
                request.getReportedMemberId(),
                request.getReason()
        );
        return ResponseEntity.ok().build();
    }

    // 신고 목록 조회 (관리자)
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getReports(
            @RequestParam(required = false) String status
    ) {
        List<ReportResponse> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody ReportStatusUpdateRequest request
    ) {
        reportService.updateReportStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

}
