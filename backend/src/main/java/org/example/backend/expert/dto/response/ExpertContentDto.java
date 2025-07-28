package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpertContentDto {
    private Long contentId;         // 컨텐츠 ID
    private String thumbnailUrl;    // 컨텐츠 썸네일 URL
    private String title;           // 컨텐츠 제목
    private String category;        // 컨텐츠 카테고리

    @QueryProjection
    public ExpertContentDto(Long contentId, String thumbnailUrl, String title, String category) {
        this.contentId = contentId;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.category = category;
    }
}
