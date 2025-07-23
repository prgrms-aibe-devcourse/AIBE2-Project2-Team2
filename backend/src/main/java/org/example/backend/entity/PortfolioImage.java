package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "portfolio_images")
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_image_id")
    private Long portfolioImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "thumbnail_check", nullable = false)
    private boolean thumbnailCheck;

    public PortfolioImage(Portfolio portfolio, String imageUrl, boolean thumbnailCheck) {
        this.portfolio = portfolio;
        this.imageUrl = imageUrl;
        this.thumbnailCheck = thumbnailCheck;
    }

    public void setThumbnailCheck(Boolean thumbnailCheck) {
        this.thumbnailCheck = thumbnailCheck;
    }

}
