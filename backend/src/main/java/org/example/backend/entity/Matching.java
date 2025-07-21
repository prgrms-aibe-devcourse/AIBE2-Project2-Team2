package org.example.backend.entity;

import lombok.Getter;
import org.example.backend.constant.MatchingStatus;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

    @OneToMany(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "matching", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    /**
     * 생성자: 매칭 요청 생성 시 필요한 필드만 설정
     */
    public Matching(Member member, Content content, String estimateUrl, MatchingStatus status) {
        this.member = member;
        this.content = content;
        this.estimateUrl = estimateUrl;
        this.status = status;
    }

    /**
     * 상태 변경 메서드: setter 없이 변경할 수 있도록
     */
    public void changeStatus(MatchingStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * JPA 기본 생성자 (protected로 설정)
     */
    protected Matching() {
    }
}
