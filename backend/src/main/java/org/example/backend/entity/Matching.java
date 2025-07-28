package org.example.backend.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import org.example.backend.constant.MatchingStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "Matching")
public class Matching extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Enumerated(EnumType.STRING)
    private MatchingStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private String rejectedReason;

    @Builder.Default
    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private EstimateRecord estimateRecord;

    public Matching(Member member, Content content, MatchingStatus status, LocalDate startDate, LocalDate endDate) {
        this.member = member;
        this.content = content;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    // ----- 상태 전이 및 도메인 메서드 -----
    public void cancel() { this.status = MatchingStatus.CANCELLED; }
    public void accept() { this.status = MatchingStatus.ACCEPTED; }
    public void rejectByExpert(String reason) {
        this.status = MatchingStatus.REJECTED;
        this.rejectedReason = reason;
    }
    public void changeStatus(MatchingStatus status) { this.status = status; }
    public void startWork() {
        this.status = MatchingStatus.IN_PROGRESS;
        this.startDate = LocalDate.now(); // 작업 시작 날짜 저장
    }
    public void completeWork() {
        this.status = MatchingStatus.WORK_COMPLETED;
        this.endDate = LocalDate.now(); // 작업 완료 날짜 저장
    }

    public void confirmCompletion() { this.status = MatchingStatus.CONFIRMED; }
    public String getRejectedReason() { return this.rejectedReason; }
    public void setStatus(MatchingStatus status) {
        this.status = status;
    }
}
