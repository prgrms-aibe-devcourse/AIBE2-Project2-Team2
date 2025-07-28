package org.example.backend.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.backend.constant.ReportStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
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

    @Column(length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ReportStatus reportStatus = ReportStatus.SUBMITTED;

    // 신고 내용 처리 일시
    private LocalDateTime resolvedAt;

    // 처리한 운영자 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private Member resolver;

    @Column(columnDefinition = "TEXT")
    private String resolverComment;

}
