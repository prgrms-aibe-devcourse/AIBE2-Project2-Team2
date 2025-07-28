package org.example.backend.content.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ContentDetailResponseDto {
    private Long contentId;
    private String title;
    private String description;
    private Long budget;
    private Long categoryId;
    private String categoryName; // 카테고리 이름 추가
    private Long expertId;
    private String expertEmail; // 전문가 이메일 추가
    private String expertNickname; // 전문가 닉네임 추가
    private String expertProfileImageUrl; // 전문가 프로필 이미지 URL 추가
    //private Long reviewId;
    private List<QuestionDto> questions;
    private String contentUrl; // 대표 이미지 URL
    private List<String> imageUrls; // 모든 이미지 URL 리스트 추가
    private List<SimplePortfolioDto> portfolios;
    private String status; // 콘텐츠 상태 추가

    @Getter
    @Builder
    public static class QuestionDto {
        private Long questionId;
        private String questionText;
        private Boolean isMultipleChoice;
        private List<OptionDto> options;

        @Getter
        @Builder
        public static class OptionDto {
            private Long optionId;
            private String optionText;
            private Long additionalPrice;
        }
    }

    @Getter
    @Builder
    public static class SimplePortfolioDto {
        private Long portfolioId;
        private String title;
        private String thumbnailUrl;
    }
}