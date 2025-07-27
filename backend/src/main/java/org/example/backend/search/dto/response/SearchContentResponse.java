package org.example.backend.search.dto.response;

import lombok.Data;
import org.example.backend.entity.Category;

@Data
public class SearchContentResponse {
    // 카테고리 이름
    private String categoryName;

    // 컨텐츠 썸네일 정보
    private Long contentId;
    private String title;
    private Long budget;

    private String contentThumbnailUrl; // 이건 컨텐츠 id로 이미지 정보 조회해서 가져오기

    // 전문가 정보
    private String expertName;
    private Long reviewCount;
    private Double rating;
}
