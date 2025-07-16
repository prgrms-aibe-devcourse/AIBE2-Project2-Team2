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


    public ExpertProfile(Member member, String introduction, String region,
                         Integer totalCareerYears, String education, Integer employeeCount) {
        this.member = member;
        this.introduction = introduction;
        this.region = region;
        this.totalCareerYears = totalCareerYears;
        this.education = education;
        this.employeeCount = employeeCount;
    }

}
