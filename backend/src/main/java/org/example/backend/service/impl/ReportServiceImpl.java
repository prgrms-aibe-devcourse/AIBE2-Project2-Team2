package org.example.backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.config.SecurityUtil;
import org.example.backend.constant.Role;
import org.example.backend.entity.Member;
import org.example.backend.entity.Report;
import org.example.backend.constant.ReportStatus;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.ReportRepository;
import org.example.backend.service.ReportService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.example.backend.dto.ReportResponse;
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
    public void submitReport(Long reporterId, Long reportedId, String reason) {
        Member reporter = memberRepository.findById(reporterId)
                .orElseThrow(() -> new IllegalArgumentException("신고자 없음"));

        Member reported = memberRepository.findById(reportedId)
                .orElseThrow(() -> new IllegalArgumentException("피신고자 없음"));

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
            ReportStatus parsedStatus = ReportStatus.valueOf(status.toUpperCase());
            reports = reportRepository.findByReportStatus(parsedStatus);
        }

        return reports.stream().map(report -> ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getMemberId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedId(report.getReported().getMemberId())
                .reportedNickname(report.getReported().getNickname())
                .reason(report.getReason())
                .status(report.getReportStatus())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateReportStatus(Long reportId, String newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고 없음"));

        ReportStatus status = ReportStatus.valueOf(newStatus.toUpperCase());
        report.setReportStatus(status);

        // 상태가 COMPLETED일 경우에만 처리자와 시간 설정
        if (status == ReportStatus.COMPLETED) {
            report.setResolvedAt(LocalDateTime.now());

            // 현재 로그인한 사용자 정보 가져오기
            String email = SecurityUtil.getCurrentUsername();
            Member resolver = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자 정보 없음"));

            if (resolver.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("신고 처리는 관리자만 할 수 있습니다.");
            }

            report.setResolver(resolver);
        }
    }


}
