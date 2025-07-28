package org.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.Status;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "review")
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;   // 매칭 정보 (리뷰 대상 매칭)

    private Double rating;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String comment;

    @OneToOne(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private ReviewImage reviewImage;

    @Enumerated(EnumType.STRING)
    private Status status;

    public  Review(Matching matching,Double rating, String comment) {
        this.matching = matching;
        this.rating = rating;
        this.comment = comment;
        this.status = Status.ACTIVE; // 기본 상태를 ACTIVE로 설정
    }

    public void markAsDeleted() {
        this.status = Status.DELETED;
    }

    public void removeImage() {
        this.reviewImage = null;
    }
}
