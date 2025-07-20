package org.example.backend.repository;

import org.example.backend.constant.JoinType;
import org.example.backend.constant.Role;
import org.example.backend.constant.Status;
import org.example.backend.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 실제 DB 사용 시
class ExpertProfileRepositoryImplTest {

    @Autowired
    @Qualifier("expertProfileRepositoryImpl") // Impl 클래스 이름을 명시 (기본 클래스명)
    private ExpertProfileRepositoryCustom expertProfileRepositoryCustom;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExpertProfileRepository expertProfileRepository;

    @Autowired
    private SpecialtyRepository specialtyRepository;

    @Autowired
    private DetailFieldRepository detailFieldRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ExpertProfileSpecialtyDetailRepository expertProfileSpecialtyDetailRepository;

    @Autowired
    private SkillCategoryRepository skillCategoryRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PortfolioImageRepository portfolioImageRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ContentImageRepository contentImageRepository;

    private Member member;
    private ExpertProfile expertProfile;

    @BeforeEach
    void setUp() throws Exception {
        // Member 생성 및 저장
        this.member = new Member();
        setField(member, "email", "testuser@example.com");
        setField(member, "nickname", "tester");
        setField(member, "phone", "01012345678");
        setField(member, "joinType", JoinType.NORMAL);
        setField(member, "role", Role.USER);
        setField(member, "status", Status.ACTIVE);
        memberRepository.save(member);

        this.expertProfile = new ExpertProfile();
        setField(expertProfile, "member", member);
        setField(expertProfile, "introduction", "자기소개");
        setField(expertProfile, "region", "서울");
        setField(expertProfile, "totalCareerYears", 5);
        setField(expertProfile, "websiteUrl", "https://site.com");
        setField(expertProfile, "facebookUrl", "https://facebook.com");
        setField(expertProfile, "instagramUrl", "https://instagram.com");
        setField(expertProfile, "xUrl", "https://x.com");
        setField(expertProfile, "reviewCount", 10L);
        setField(expertProfile, "rating", 4.5);
        expertProfileRepository.save(expertProfile);

        // Specialty 조회 (시더 데이터에 반드시 존재)
        Specialty specialty = specialtyRepository.findByName("디자인")
                .orElseThrow(() -> new IllegalStateException("Specialty '디자인'이 존재하지 않습니다."));

        // DetailField 조회 (시더 데이터에 반드시 존재)
        DetailField detailField = detailFieldRepository.findByNameAndSpecialty("웹/모바일 디자인", specialty)
                .orElseThrow(() -> new IllegalStateException("DetailField '웹/모바일 디자인'이 존재하지 않습니다."));

        // ExpertProfileSpecialtyDetail 저장
        ExpertProfileSpecialtyDetail epsd = new ExpertProfileSpecialtyDetail(expertProfile, specialty, detailField);
        expertProfileSpecialtyDetailRepository.save(epsd);

        // SkillCategory 조회 (시더 데이터에 반드시 존재)
        SkillCategory skillCategory = skillCategoryRepository.findByName("IT/프로그래밍")
                .orElseThrow(() -> new IllegalStateException("SkillCategory 'IT/프로그래밍'가 존재하지 않습니다."));

        // Skill 조회 (시더 데이터에 반드시 존재)
        Skill skill = skillRepository.findByNameAndCategory("Java", skillCategory)
                .orElseThrow(() -> new IllegalStateException("Skill 'Java'가 존재하지 않습니다."));

        // expertProfile.skills에 skill 추가
        expertProfile.getSkills().add(skill);
        // 저장
        expertProfileRepository.save(expertProfile);


        // Portfolio 생성 및 저장
        Portfolio portfolio = new Portfolio(expertProfile, "포트폴리오 제목", "내용", 3, "카테고리");
        portfolioRepository.save(portfolio);

        // PortfolioImage 생성 및 저장 (썸네일)
        PortfolioImage portfolioImage = new PortfolioImage(portfolio, "https://thumbnail.url/portfolio1.jpg");
        portfolioImageRepository.save(portfolioImage);

        // Content 생성 및 저장
        Content content = new Content();
        setField(content, "member", member);
        setField(content, "title", "컨텐츠 제목");
        setField(content, "category", "카테고리");
        contentRepository.save(content);

        // ContentImage 생성 및 저장 (orderIndex=0 썸네일)
        ContentImage contentImage = new ContentImage();
        setField(contentImage, "content", content);
        setField(contentImage, "imageUrl", "https://thumbnail.url/content1.jpg");
        setField(contentImage, "orderIndex", (byte)0);
        contentImageRepository.save(contentImage);
    }

    // 리플렉션으로 필드 직접 주입 메서드
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void findExpertProfileByEmail_통합_테스트() {
        String email = "testuser@example.com";
        var dto = expertProfileRepositoryCustom.findExpertProfileByEmail(email);

        assertNotNull(dto);
        assertEquals("tester", dto.getNickname());
        assertEquals("자기소개", dto.getIntroduction());
        assertEquals("서울", dto.getRegion());
        assertEquals(5, dto.getTotalCareerYears());
        assertEquals("https://site.com", dto.getWebsiteUrl());
        assertEquals("https://facebook.com", dto.getFacebookUrl());
        assertEquals("https://instagram.com", dto.getInstagramUrl());
        assertEquals("https://x.com", dto.getXUrl());
        assertEquals(10L, dto.getReviewCount());
        assertEquals(4.5, dto.getAverageScore());

        assertTrue(dto.getFields().stream().anyMatch(f -> "디자인".equals(f.getSpecialtyName())));
        assertTrue(dto.getFields().stream().anyMatch(f -> "웹/모바일 디자인".equals(f.getDetailFieldName())));

        assertTrue(dto.getSkills().stream().anyMatch(s -> "IT/프로그래밍".equals(s.getSkillCategoryName())));
        assertTrue(dto.getSkills().stream().anyMatch(s -> "Java".equals(s.getSkillName())));

        assertTrue(dto.getPortfolios().stream().anyMatch(p -> "포트폴리오 제목".equals(p.getTitle())));
        assertTrue(dto.getPortfolios().stream().anyMatch(p -> "https://thumbnail.url/portfolio1.jpg".equals(p.getThumbnailUrl())));

        assertTrue(dto.getContents().stream().anyMatch(c -> "컨텐츠 제목".equals(c.getTitle())));
        assertTrue(dto.getContents().stream().anyMatch(c -> "https://thumbnail.url/content1.jpg".equals(c.getThumbnailUrl())));
    }
}
