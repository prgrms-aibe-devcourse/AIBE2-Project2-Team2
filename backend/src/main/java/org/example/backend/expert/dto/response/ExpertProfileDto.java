package org.example.backend.expert.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpertProfileDto {
    // 멤버 정보
    private String profileImageUrl;     // 프로필 이미지 URL
    private String nickname;            // 닉네임

    // 전문가 정보
    private String introduction;        // 자기소개
    private String region;              // 활동 지역
    private Integer totalCareerYears;   // 경력 연수
    private String websiteUrl;          // 웹사이트 URL
    private String facebookUrl;         // 페이스북 URL
    private String instagramUrl;        // 인스타그램 URL
    private String xUrl;                // X (구 Twitter) URL
    private Long reviewCount;            // 리뷰 수
    private Double averageScore;        // 평균 평점

    // 전문 분야 정보
    private List<ExpertFieldDto> fields;

    // 전문가 기술 정보
    private List<ExpertSkillDto> skills;

    // 컨텐츠 정보
    private List<ExpertContentDto> contents;

    // 포트폴리오 정보
    private List<ExpertPortfolioDto> portfolios;

    @QueryProjection
    public ExpertProfileDto(
            String profileImageUrl,
            String nickname,
            String introduction,
            String region,
            Integer totalCareerYears,
            String websiteUrl,
            String facebookUrl,
            String instagramUrl,
            String xUrl,
            Long reviewCount,
            Double averageScore
    ) {
        this.profileImageUrl = profileImageUrl;
        this.nickname = nickname;
        this.introduction = introduction;
        this.region = region;
        this.totalCareerYears = totalCareerYears;
        this.websiteUrl = websiteUrl;
        this.facebookUrl = facebookUrl;
        this.instagramUrl = instagramUrl;
        this.xUrl = xUrl;
        this.reviewCount = reviewCount;
        this.averageScore = averageScore;
    }
}
