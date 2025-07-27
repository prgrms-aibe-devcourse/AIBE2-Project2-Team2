package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "expert_profiles")
public class ExpertProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_profile_id")
    private Long expertProfileId;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;          // 기본정보

    @Column(columnDefinition = "TEXT")
    private String introduction;        // 자기소개

    private String region;          // 활동 지역

    @Column(name = "total_career_years")
    private Integer totalCareerYears;       // 총 경력 연수

    private String education;           // 학력

    @Column(name = "employee_count")
    private Integer employeeCount;          // 직원 수

    @OneToMany(mappedBy = "expertProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpertProfileSpecialtyDetail> specialtyDetailFields = new ArrayList<>();   // 전문 분야와 세부 분야

    @ManyToMany
    @JoinTable(name = "expert_profile_skill",
            joinColumns = @JoinColumn(name = "expert_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();        // 기술 스킬

    @OneToMany(mappedBy = "expertProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers = new ArrayList<>();      // 경력

    @OneToMany(mappedBy = "expertProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Portfolio> portfolios = new ArrayList<>();     // 포트폴리오

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "x_url")
    private String xUrl;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "review_count")
    private Long reviewCount;          // 리뷰 수

    @Column(name = "rating")
    private Double rating;       // 평균 평점


    // 생성 메서드 추가
    public static ExpertProfile createExpertProfile(Member member, String introduction, String region,
                                                    Integer totalCareerYears, String education, Integer employeeCount,
                                                    String websiteUrl, String facebookUrl, String xUrl, String instagramUrl) {
        ExpertProfile profile = new ExpertProfile();
        profile.member = member;
        profile.introduction = introduction != null ? introduction : "";
        profile.region = region != null ? region : "";
        profile.totalCareerYears = totalCareerYears != null ? totalCareerYears : 0;
        profile.education = education != null ? education : "";
        profile.employeeCount = employeeCount != null ? employeeCount : 0;
        profile.websiteUrl = websiteUrl;
        profile.facebookUrl = facebookUrl;
        profile.xUrl = xUrl;
        profile.instagramUrl = instagramUrl;
        profile.reviewCount = 0L; // 초기 리뷰 수
        profile.rating = 0.0; // 초기 평점은 0.0
        return profile;
    }
    //업데이트 메서드 (변경용)
    public void updateProfileInfo(String introduction, String region, Integer totalCareerYears,
                                  String education, Integer employeeCount, String websiteUrl,
                                  String facebookUrl, String xUrl, String instagramUrl) {
        this.introduction = introduction != null ? introduction : this.introduction;
        this.region = region != null ? region : this.region;
        this.totalCareerYears = totalCareerYears != null ? totalCareerYears : this.totalCareerYears;
        this.education = education != null ? education : this.education;
        this.employeeCount = employeeCount != null ? employeeCount : this.employeeCount;
        this.websiteUrl = websiteUrl;
        this.facebookUrl = facebookUrl;
        this.xUrl = xUrl;
        this.instagramUrl = instagramUrl;
    }

    // 리뷰 수 감소 + 평점 재계산
    public void subRating(Double rating) {
        if (this.reviewCount <= 1) {
            this.rating = 0.0;
            this.reviewCount = 0L;
        } else {
            this.rating = ((this.rating * this.reviewCount) - rating) / (this.reviewCount - 1);
            this.reviewCount--;
        }
    }

    // 리뷰 수 증가 + 평점 재계산
    public void addRating(Double rating) {
        if (this.reviewCount == 0) {
            this.rating = rating;
        } else {
            this.rating = ((this.rating * this.reviewCount) + rating) / (this.reviewCount + 1);
        }
        this.reviewCount++;
    }

}
