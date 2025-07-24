package org.example.backend.controller;

import lombok.RequiredArgsConstructor;
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
import java.util.Map;

import static org.example.backend.config.SecurityUtil.getCurrentUsername;

@Tag(name = "신고 관리 API", description = "신고 등록, 조회, 상태 변경, 삭제 등 신고 관련 기능 제공")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 신고 등록 (사용자 닉네임 기반)
     */
    @Operation(summary = "신고 등록", description = "신고자의 이메일과 피신고자의 닉네임, 신고 사유를 받아 신고를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "신고자 또는 피신고자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> reportByNickname(@RequestBody Map<String, String> request) {
        String reportedNickname = request.get("reportedNickname");
        String reason = request.get("reason");
        String reporterEmail = getCurrentUsername();

        reportService.submitReportByNickname(reporterEmail, reportedNickname, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * 내가 신고한 내역 조회
     */
    @Operation(summary = "내 신고 목록 조회", description = "현재 로그인한 사용자가 신고한 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 필요"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        return ResponseEntity.ok(reportService.getReportsByCurrentUser());
    }

    /**
     * 전체 신고 목록 (상태 필터링 가능)
     */
    @Operation(summary = "신고 목록 조회", description = "신고 상태(status)에 따라 필터링된 목록을 조회합니다. status 없으면 전체 조회.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 값"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReports(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(reportService.getReportsByStatus(status));
    }

    /**
     * 특정 신고 상세 조회
     */
    @Operation(summary = "신고 상세 조회", description = "신고 ID 기반으로 상세 정보 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    /**
     * 상태만 단독 변경
     */
    @Operation(summary = "신고 상태 변경", description = "신고 상태만 변경합니다. 상태가 'COMPLETED'인 경우 처리자 및 시간도 기록됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
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
     * 상태 + 의견 동시에 수정 (상세 모달용)
     */
    @Operation(summary = "신고 상태 및 의견 수정", description = "신고 상태와 처리 의견을 함께 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateReport(
            @PathVariable Long id,
            @RequestBody ReportUpdateRequest request
    ) {
        reportService.updateStatusAndComment(id, request.getStatus(), request.getResolverComment());
        return ResponseEntity.ok().build();
    }

    /**
     * 신고 삭제
     */
    @Operation(summary = "신고 삭제", description = "신고 ID로 삭제 처리")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 신고 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
