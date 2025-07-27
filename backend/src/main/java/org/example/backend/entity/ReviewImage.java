package org.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "review_img")
@AllArgsConstructor
@NoArgsConstructor
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    private String imageUrl;

    public ReviewImage(Review review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }
}
