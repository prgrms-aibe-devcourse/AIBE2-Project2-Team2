package org.example.backend.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "전문가 리뷰 응답 DTO")
public class ReviewResponseDto {

    @Schema(description = "전문가 전체 평균 평점", example = "4.5")
    private Double totalRating;

    @Schema(description = "전문가의 전체 리뷰 수", example = "15")
    private Long totalReviewCount;

    @Schema(description = "리뷰 목록 (페이징 처리된 리뷰)", implementation = ReviewDetailDto.class)
    private Page<ReviewDetailDto> reviews;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "리뷰 상세 DTO")
    public static class ReviewDetailDto {

        @Schema(description = "리뷰 ID", example = "101")
        private Long reviewId;

        @Schema(description = "리뷰 내용", example = "전문가가 정말 친절하고 정확했어요!")
        private String comment;

        @Schema(description = "리뷰 평점 (1.0~5.0)", example = "4.0")
        private Double rating;

        @Schema(description = "작성자 닉네임", example = "happy_user")
        private String reviewerNickname;

        @Schema(description = "작성자 프로필 이미지 URL", example = "https://firebasestorage.googleapis.com/...")
        private String reviewerProfileImageUrl;

        @Schema(description = "리뷰 작성일 (yyyy-MM-dd HH:mm)", example = "2025-07-25 18:23")
        private String createdAt;

        @Schema(description = "리뷰 이미지 URL", example = "https://firebasestorage.googleapis.com/...")
        private String reviewImageUrl;
    }
}
