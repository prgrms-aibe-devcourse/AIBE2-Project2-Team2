package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.dto.ReportRequest;
import org.example.backend.dto.ReportResponse;
import org.example.backend.dto.ReportStatusUpdateRequest;
import org.example.backend.dto.ReportUpdateRequest;
import org.example.backend.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "신고 관리 API", description = "신고 등록, 조회, 상태 변경, 삭제 등 신고 관련 기능 제공")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 등록 API
     * User, Admin 모두 접근 가능
     */
    @Operation(summary = "신고 등록", description = "신고자 ID와 피신고자 ID, 사유를 받아 새로운 신고를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 유효하지 않은 사용자 ID"),
            @ApiResponse(responseCode = "404", description = "신고자 또는 피신고자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    public ResponseEntity<Void> reportMember(@RequestBody ReportRequest request) {
        reportService.submitReport(
                request.getReporterId(),
                request.getReportedMemberId(),
                request.getReason()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * 내가 신고한 목록 조회 API
     * 로그인된 사용자 기준으로 본인이 작성한 신고 내역을 조회
     */
    @Operation(summary = "내가 신고한 목록 조회", description = "현재 로그인한 사용자가 신고한 모든 신고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        List<ReportResponse> reports = reportService.getReportsByCurrentUser();
        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 목록 조회 (관리자 전용)
     * 상태(status) 파라미터로 필터링 가능
     */
    @Operation(summary = "신고 목록 조회", description = "신고 상태(status)에 따라 필터링된 신고 목록을 조회합니다. status 값이 없으면 전체 조회됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 status 값"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReports(@RequestParam(required = false) String status) {
        List<ReportResponse> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    /**
     * 신고 상세 조회 (관리자 전용)
     */
    @Operation(summary = "신고 상세 조회", description = "신고 ID를 기반으로 해당 신고의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        ReportResponse report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * 신고 상태 변경 (관리자 전용)
     * 'COMPLETED'로 변경 시 처리자, 처리시간 기록
     */
    @Operation(summary = "신고 상태 변경", description = "신고 ID를 기준으로 신고 상태를 변경합니다. 상태가 'COMPLETED'일 경우, 처리자와 처리시간도 함께 기록됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 값"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "신고 또는 관리자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody ReportStatusUpdateRequest request
    ) {
        reportService.updateReportStatus(id, request.getStatus());
        return ResponseEntity.ok().build();
    }

    /**
     * 신고 삭제 (관리자 전용)
     */
    @Operation(summary = "신고 삭제", description = "지정한 신고 ID의 신고를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReport(
            @PathVariable Long id,
            @RequestBody ReportUpdateRequest request
    ) {
        reportService.updateStatusAndComment(id, request.getStatus(), request.getResolverComment());
        return ResponseEntity.ok().build();
    }




}