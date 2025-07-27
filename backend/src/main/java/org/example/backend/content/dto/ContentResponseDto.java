package org.example.backend.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.entity.Content;
import org.example.backend.entity.ContentImage;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponseDto {

    @Schema(description = "컨텐츠 ID", example = "1")
    private Long contentId;

    @Schema(description = "작성자(회원) ID", example = "3")
    private Long memberId;

    @Schema(description = "컨텐츠 제목", example = "로고 디자인")
    private String title;

    @Schema(description = "컨텐츠 설명", example = "브랜드 맞춤형 로고 디자인 서비스")
    private String description;

    @Schema(description = "예산", example = "150000")
    private Long budget;

    @Schema(description = "컨텐츠 상태", example = "ACTIVE")
    private String status;

    @Schema(description = "등록 일시", example = "2024-07-25T10:30:00")
    private String regTime;

    @Schema(description = "수정 일시", example = "2024-07-25T10:30:00")
    private String updateTime;

    @Schema(description = "작성자 이메일", example = "expert@example.com")
    private String createdBy;

    @Schema(description = "수정자 이메일", example = "admin@example.com")
    private String modifiedBy;

    @Schema(description = "카테고리 ID", example = "5")
    private Long categoryId;

    @Schema(description = "카테고리 이름", example = "로고 디자인")
    private String categoryName;

    @Schema(description = "대표 이미지(썸네일) URL", example = "https://example.com/thumb.jpg")
    private String contentUrl;

    @Schema(description = "전체 이미지 URL 리스트")
    private List<String> imageUrls;

    @Schema(description = "전문가 이름", example = "김전문")
    private String expertName;

    @Schema(description = "전문가 평균 평점", example = "4.7")
    private Double rating;

    @Schema(description = "전문가 리뷰 수", example = "23")
    private Long reviewCount;

    public static ContentResponseDto from(Content content) {
        List<String> imageUrls = content.getImages() != null
                ? content.getImages().stream()
                .map(ContentImage::getImageUrl)
                .collect(Collectors.toList())
                : List.of();

        String contentUrl = null;
        if (content.getImages() != null && !content.getImages().isEmpty()) {
            contentUrl = content.getImages().stream()
                    .filter(ContentImage::isThumbnail)
                    .map(ContentImage::getImageUrl)
                    .findFirst()
                    .orElse(content.getImages().get(0).getImageUrl());
        }

        String expertName = null;
        Double rating = null;
        Long reviewCount = null;
        if (content.getMember() != null && content.getMember().getExpertProfile() != null) {
            expertName = content.getMember().getNickname();
            rating = content.getMember().getExpertProfile().getRating();
            reviewCount = content.getMember().getExpertProfile().getReviewCount();
        }

        return ContentResponseDto.builder()
                .contentId(content.getContentId())
                .memberId(content.getMember().getMemberId())
                .title(content.getTitle())
                .description(content.getDescription())
                .budget(content.getBudget())
                .status(content.getStatus() != null ? content.getStatus().name() : null)
                .regTime(content.getRegTime() != null ? content.getRegTime().toString() : null)
                .updateTime(content.getUpdateTime() != null ? content.getUpdateTime().toString() : null)
                .createdBy(content.getCreatedBy())
                .modifiedBy(content.getModifiedBy())
                .categoryId(content.getCategory() != null ? content.getCategory().getCategoryId() : null)
                .categoryName(content.getCategory() != null ? content.getCategory().getName() : null)
                .imageUrls(imageUrls)
                .contentUrl(contentUrl)
                .expertName(expertName)
                .rating(rating)
                .reviewCount(reviewCount)
                .build();
    }
}