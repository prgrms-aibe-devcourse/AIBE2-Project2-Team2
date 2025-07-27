package org.example.backend.repository;

import org.example.backend.constant.ReportStatus;
import org.example.backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByReportStatus(ReportStatus status);
    List<Report> findByReporterEmail(String email);

}
