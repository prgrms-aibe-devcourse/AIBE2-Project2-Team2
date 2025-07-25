package org.example.backend.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.backend.entity.Career;
import org.example.backend.entity.ExpertProfile;
import org.example.backend.expert.dto.response.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.example.backend.entity.QContent.content;
import static org.example.backend.entity.QContentImage.contentImage;
import static org.example.backend.entity.QDetailField.detailField;
import static org.example.backend.entity.QExpertProfile.expertProfile;
import static org.example.backend.entity.QExpertProfileSpecialtyDetail.expertProfileSpecialtyDetail;
import static org.example.backend.entity.QMember.member;
import static org.example.backend.entity.QPortfolio.portfolio;
import static org.example.backend.entity.QPortfolioImage.portfolioImage;
import static org.example.backend.entity.QSkill.skill;
import static org.example.backend.entity.QSkillCategory.skillCategory;
import static org.example.backend.entity.QSpecialty.specialty;

@RequiredArgsConstructor
public class ExpertProfileRepositoryImpl implements ExpertProfileRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public ExpertProfileDto findExpertProfileByEmail(String email) {
        // 1. 기본 프로필 + 멤버
        ExpertProfile profile = queryFactory
                .selectFrom(expertProfile)
                .join(expertProfile.member, member).fetchJoin()
                .where(member.email.eq(email))
                .fetchOne();

        if (profile == null) {
            return null;
        }

        // 2. 분야 정보 (전문 분야별 상세 분야 그룹핑)
        List<ExpertSpecialtyDto> specialties = queryFactory
                .select(
                        specialty.name,
                        detailField.name
                )
                .from(expertProfileSpecialtyDetail)
                .join(expertProfileSpecialtyDetail.specialty, specialty)
                .join(expertProfileSpecialtyDetail.detailField, detailField)
                .where(expertProfileSpecialtyDetail.expertProfile.eq(profile))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(0, String.class), // specialty name
                        Collectors.mapping(
                                tuple -> tuple.get(1, String.class), // detail field name
                                Collectors.toList()
                        )
                ))
                .entrySet().stream()
                .map(e -> ExpertSpecialtyDto.builder()
                        .specialty(e.getKey())
                        .detailFields(e.getValue())
                        .build())
                .collect(Collectors.toList());


        // 3. 기술 정보
        List<ExpertSkillDto> skills = queryFactory
                .select(new QExpertSkillDto(
                        skill.category.name,
                        skill.name
                ))
                .from(expertProfile)
                .join(expertProfile.skills, skill)
                .join(skill.category, skillCategory)
                .where(expertProfile.eq(profile))
                .fetch();


        // 4. 콘텐츠 정보
        List<ExpertContentDto> contents = queryFactory
                .select(new QExpertContentDto(
                        content.contentId,
                        contentImage.imageUrl,  // 첫번째 이미지 URL
                        content.title,
                        content.category.name // 카테고리 이름만 나오도록 수정
                ))
                .from(content)
                .leftJoin(contentImage)
                .on(contentImage.content.eq(content)
                        .and(contentImage.orderIndex.eq((byte)0))) // orderIndex 0번 이미지만 조인
                .where(content.member.eq(profile.getMember()))
                .fetch();


        // 5. 포트폴리오 정보
        List<ExpertPortfolioDto> portfolios = queryFactory
                .select(new QExpertPortfolioDto(
                        portfolio.portfolioId,
                        portfolioImage.imageUrl,
                        portfolio.title
                ))
                .from(portfolio)
                .leftJoin(portfolio.images, portfolioImage)
                .on(portfolioImage.thumbnailCheck.eq(true))
                .where(portfolio.expertProfile.eq(profile))
                .fetch();

        // 6. 경력 설명 문자열 리스트
        List<String> careers = profile.getCareers().stream()
                .map(Career::getDescription)
                .collect(Collectors.toList());

        // 7. 최종 ExpertProfileDto 반환
        return ExpertProfileDto.builder()
                .profileImageUrl(profile.getMember().getProfileImageUrl())
                .nickname(profile.getMember().getNickname())
                .introduction(profile.getIntroduction())
                .region(profile.getRegion())
                .totalCareerYears(profile.getTotalCareerYears())
                .education(profile.getEducation())
                .employeeCount(profile.getEmployeeCount())
                .websiteUrl(profile.getWebsiteUrl())
                .facebookUrl(profile.getFacebookUrl())
                .xUrl(profile.getXUrl())
                .instagramUrl(profile.getInstagramUrl())
                .reviewCount(profile.getReviewCount())
                .averageScore(profile.getRating())
                .specialties(specialties)
                .skills(skills)
                .careers(careers)
                .contents(contents)
                .portfolios(portfolios)
                .build();
    }
}