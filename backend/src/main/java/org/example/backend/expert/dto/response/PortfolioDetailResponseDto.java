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

    // 썸네일 이미지 (포트폴리오 이미지 중 thumbnailCheck가 true인 이미지)
    private PortfolioImageDto thumbnailImage;

    // 전문가 정보
    private String expertNickname;
    private String expertProfileImageUrl;
    private Long reviewCount;
    private Double rating;

    @Getter
    @AllArgsConstructor
    public static class PortfolioImageDto {
        private Long id;
        private String url;
    }
}
