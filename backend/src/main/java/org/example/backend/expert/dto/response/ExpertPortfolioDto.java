package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpertPortfolioDto {
    private Long portfolioId;       // 포트폴리오 ID
    private String thumbnailUrl;    // 포트폴리오 썸네일 URL
    private String title;           // 포트폴리오 제목
    private String category;        // 포트폴리오 카테고리

    @QueryProjection
    public ExpertPortfolioDto(Long portfolioId, String thumbnailUrl, String title, String category) {
        this.portfolioId = portfolioId;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.category = category;
    }
}