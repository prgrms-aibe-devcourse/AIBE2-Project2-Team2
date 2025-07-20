package org.example.backend.expert.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class PortfolioDetailResponseDto {

    private Long portfolioId;
    private String title;
    private String content;
    private Long viewCount;
    private Integer workingYear;
    private String category;

    private List<PortfolioImageDto> images;

    // 전문가 정보
    private Long reviewCount;
    private Double rating;

    @Getter
    @AllArgsConstructor
    public static class PortfolioImageDto {
        private Long id;
        private String url;
    }
}
