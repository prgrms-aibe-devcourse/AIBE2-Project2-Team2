package org.example.backend.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.MatchingStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)@Table(name = "Matching")
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

    public void setStatus(MatchingStatus status) {
        this.status = status;
    }
}