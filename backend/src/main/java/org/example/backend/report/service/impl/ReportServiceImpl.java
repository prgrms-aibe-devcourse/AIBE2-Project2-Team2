package org.example.backend.report.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.constant.ReportStatus;
import org.example.backend.constant.Role;
import org.example.backend.entity.Member;
import org.example.backend.entity.Report;
import org.example.backend.exception.customException.InvalidReportStatusException;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.exception.customException.ReportNotFoundException;
import org.example.backend.report.dto.ReportResponse;
import org.example.backend.report.service.ReportService;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.ReportRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    @Override
    public void submitReportByNickname(String reporterEmail, String reportedNickname, String reason) {
        Member reporter = findMemberByEmail(reporterEmail);
        Member reported = memberRepository.findByNickname(reportedNickname)
                .orElseThrow(() -> new MemberNotFoundException("피신고자 정보를 찾을 수 없습니다."));

        String category = extractCategory(reason);

        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        report.setCategory(category);
        report.setReportStatus(ReportStatus.SUBMITTED);

        reportRepository.save(report);
    }

    @Override
    public void submitReport(Long reporterId, Long reportedId, String reason) {
        Member reporter = findMemberById(reporterId);
        Member reported = findMemberById(reportedId);

        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        report.setReportStatus(ReportStatus.SUBMITTED);

        reportRepository.save(report);
    }

    @Override
    public List<ReportResponse> getReportsByStatus(String status) {
        List<Report> reports = (status == null || status.isBlank())
                ? reportRepository.findAll()
                : reportRepository.findByReportStatus(parseStatus(status));

        return reports.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));
        return convertToResponse(report);
    }

    @Override
    public void updateReportStatus(Long reportId, String newStatus, String email) {
        Report report = getReportEntity(reportId);
        ReportStatus status = parseStatus(newStatus);
        report.setReportStatus(status);

        handleResolutionMetadata(report, status,email);
    }

    @Override
    public void updateStatusAndComment(Long reportId, String statusStr, String resolverComment,String email) {
        Report report = getReportEntity(reportId);
        ReportStatus status = parseStatus(statusStr);

        report.setReportStatus(status);
        report.setResolverComment(resolverComment);

        handleResolutionMetadata(report, status, email);
    }

    @Override
    public void deleteReport(Long reportId) {
        Report report = getReportEntity(reportId);
        reportRepository.delete(report);
    }

    @Override
    public List<ReportResponse> getReportsByCurrentUser(String email) {
        return reportRepository.findByReporterEmail(email).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /** 신고 상태 문자열 → Enum 변환 */
    private ReportStatus parseStatus(String statusStr) {
        try {
            return ReportStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidReportStatusException("유효하지 않은 신고 상태입니다: " + statusStr);
        }
    }

    /** 신고 사유에서 카테고리 추출 */
    private String extractCategory(String reason) {
        if (reason != null && reason.contains(":")) {
            return reason.split(":")[0].trim();
        }
        return "기타";
    }

    /** 관리자 처리자 정보, 처리 시간 기록 */
    private void handleResolutionMetadata(Report report, ReportStatus status, String email) {
        if (status == ReportStatus.IN_PROGRESS || status == ReportStatus.COMPLETED) {
            report.setResolvedAt(LocalDateTime.now());
            Member resolver = findMemberByEmail(email);

            if (resolver.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("신고 처리는 관리자만 할 수 있습니다.");
            }

            report.setResolver(resolver);
        }
    }

    /** 신고 엔티티 조회 */
    private Report getReportEntity(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));
    }

    /** 회원 ID 기반 조회 */
    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));
    }

    /** 회원 이메일 기반 조회 */
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));
    }

    /** Entity → DTO 변환 */
    private ReportResponse convertToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getMemberId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedId(report.getReported().getMemberId())
                .reportedNickname(report.getReported().getNickname())
                .reason(report.getReason())
                .category(report.getCategory())
                .createdAt(report.getRegTime())
                .status(report.getReportStatus())
                .resolvedAt(report.getResolvedAt())
                .resolverNickname(report.getResolver() != null ? report.getResolver().getNickname() : null)
                .resolverComment(report.getResolverComment())
                .build();
    }
}
