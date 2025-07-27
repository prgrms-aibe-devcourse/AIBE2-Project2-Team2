package org.example.backend.entity;

import lombok.Getter;
import org.example.backend.constant.ReportStatus;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "report")
public class Report extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_Id", nullable = false)
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_Id", nullable = false)
    private Member reported;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus;
}
