package org.example.backend.content.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentResponseDto {
    private Long contentId;
    private Long memberId;
    private String title;
    private String description;
    private Long budget;
    private String status;
    private String regTime;
    private String updateTime;
    private String createdBy;
    private String modifiedBy;
    private Long categoryId;
    private String categoryName;
    private java.util.List<String> imageUrls;
    private String contentUrl; // 대표 이미지 URL
}
