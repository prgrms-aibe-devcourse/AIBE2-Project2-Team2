package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.config.SecurityUtil;
import org.example.backend.constant.Role;
import org.example.backend.constant.ReportStatus;
import org.example.backend.dto.ReportResponse;
import org.example.backend.entity.Member;
import org.example.backend.entity.Report;
import org.example.backend.exception.customException.InvalidReportStatusException;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.exception.customException.ReportNotFoundException;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.ReportRepository;
import org.example.backend.service.ReportService;
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

    // ✅ 닉네임 기반 신고 등록 추가
    @Override
    public void submitReportByNickname(String reporterEmail, String reportedNickname, String reason) {
        Member reporter = memberRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new MemberNotFoundException("신고자 정보를 찾을 수 없습니다."));
        Member reported = memberRepository.findByNickname(reportedNickname)
                .orElseThrow(() -> new MemberNotFoundException("피신고자 정보를 찾을 수 없습니다."));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        report.setReportStatus(ReportStatus.SUBMITTED);

        reportRepository.save(report);
    }

    @Override
    public void submitReport(Long reporterId, Long reportedId, String reason) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new MemberNotFoundException("신고자 정보를 찾을 수 없습니다."));
        Member reported = memberRepository.findById(reportedId)
                .orElseThrow(() -> new MemberNotFoundException("피신고자 정보를 찾을 수 없습니다."));

        Report report = new Report();
        report.setReporter(reporter);
        report.setReported(reported);
        report.setReason(reason);
        report.setReportStatus(ReportStatus.SUBMITTED);

        reportRepository.save(report);
    }

    @Override
    public List<ReportResponse> getReportsByStatus(String status) {
        List<Report> reports;
        if (status == null || status.isBlank()) {
            reports = reportRepository.findAll();
        } else {
            ReportStatus parsedStatus = parseStatus(status);
            reports = reportRepository.findByReportStatus(parsedStatus);
        }

        return reports.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public void updateReportStatus(Long reportId, String newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));

        ReportStatus status = parseStatus(newStatus);
        report.setReportStatus(status);

        handleAdminResolutionIfNeeded(report, status);
    }

    @Override
    public void updateStatusAndComment(Long reportId, String statusStr, String resolverComment) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));

        ReportStatus status = parseStatus(statusStr);
        report.setReportStatus(status);
        report.setResolverComment(resolverComment);

        handleAdminResolutionIfNeeded(report, status);
    }

    @Override
    public void deleteReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));
        reportRepository.delete(report);
    }

    @Override
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ReportNotFoundException("해당 신고가 존재하지 않습니다."));

        return convertToResponse(report);
    }

    @Override
    public List<ReportResponse> getReportsByCurrentUser() {
        String email = SecurityUtil.getCurrentUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("사용자 정보를 찾을 수 없습니다."));

        List<Report> reports = reportRepository.findByReporterEmail(email);

        return reports.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    private ReportStatus parseStatus(String statusStr) {
        try {
            return ReportStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidReportStatusException("유효하지 않은 신고 상태입니다: " + statusStr);
        }
    }

    private void handleAdminResolutionIfNeeded(Report report, ReportStatus status) {
        if (status == ReportStatus.IN_PROGRESS || status == ReportStatus.COMPLETED) {
            report.setResolvedAt(LocalDateTime.now());

            String email = SecurityUtil.getCurrentUsername();
            Member resolver = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new MemberNotFoundException("현재 로그인된 사용자 정보를 찾을 수 없습니다."));

            if (resolver.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("신고 처리는 관리자만 할 수 있습니다.");
            }

            report.setResolver(resolver);
        }
    }

    private ReportResponse convertToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getMemberId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedId(report.getReported().getMemberId())
                .reportedNickname(report.getReported().getNickname())
                .reason(report.getReason())
                .status(report.getReportStatus())
                .resolvedAt(report.getResolvedAt())
                .resolverNickname(report.getResolver() != null ? report.getResolver().getNickname() : null)
                .resolverComment(report.getResolverComment())
                .build();
    }
}
