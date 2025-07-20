package org.example.backend.expert;

import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.expert.dto.request.ExpertRequestDto;
import org.example.backend.expert.dto.request.SkillDto;
import org.example.backend.expert.dto.request.SpecialtyDetailRequestDto;
import org.example.backend.expert.dto.response.PortfolioDetailResponseDto;
import org.example.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ExpertServiceTest {

    @InjectMocks
    private ExpertService expertService;

    @Mock private MemberRepository memberRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private SpecialtyRepository specialtyRepository;
    @Mock private DetailFieldRepository detailFieldRepository;
    @Mock private ExpertProfileSpecialtyDetailRepository expertProfileSpecialtyDetailRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private CareerRepository careerRepository;
    @Mock private SkillCategoryRepository skillCategoryRepository;
    @Mock private PortfolioRepository portfolioRepository;

    @Mock private ExpertProfileRepositoryCustom expertProfileRepositoryCustom;

    private Member testMember;
    private ExpertRequestDto requestDto;

    @BeforeEach
    void setup() throws Exception {
        testMember = new Member();

        Field emailField = Member.class.getDeclaredField("email");
        emailField.setAccessible(true);
        emailField.set(testMember, "test@example.com");

        Field roleField = Member.class.getDeclaredField("role");
        roleField.setAccessible(true);
        roleField.set(testMember, Role.USER);

        // 새로운 SpecialtyDetailRequestDto 리스트 생성
        List<SpecialtyDetailRequestDto> specialtyDetailList = List.of(
                new SpecialtyDetailRequestDto("디자인", List.of("웹/모바일 디자인", "마케팅 디자인"))
                // 필요하면 다른 전문분야 추가 가능
        );

        // SkillDto 리스트 생성 (기존 List<String> -> List<SkillDto>)
        List<SkillDto> skillDtoList = List.of(
                new SkillDto("IT/프로그래밍", "Java"),
                new SkillDto("IT/프로그래밍", "Spring")
                // 필요하면 다른 스킬 추가 가능
        );

        requestDto = new ExpertRequestDto(
                specialtyDetailList,
                "소개글",
                "서울 강남구",
                5,
                "서울대",
                10,
                "https://site.com",
                "https://facebook.com",
                "https://x.com",
                "https://instagram.com",
                skillDtoList,           // 수정됨
                List.of("3년 경력 있음")
        );
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void upgradeToExpert_정상작동() {
        // given
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));
        when(expertProfileRepository.findByMember(any())).thenReturn(Optional.empty());

        // 더미 Specialty 객체 생성
        Specialty dummySpecialty = new Specialty("디자인");
        when(specialtyRepository.findByName("디자인"))
                .thenReturn(Optional.of(dummySpecialty));

        when(detailFieldRepository.findByName("웹/모바일 디자인"))
                .thenReturn(Optional.of(new DetailField("웹/모바일 디자인", dummySpecialty)));

        when(detailFieldRepository.findByName("마케팅 디자인"))
                .thenReturn(Optional.of(new DetailField("마케팅 디자인", dummySpecialty)));

        when(skillRepository.findByName(any())).thenReturn(Optional.empty());
        when(skillRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        expertService.upgradeToExpert("test@example.com", requestDto);

        // then
        assertEquals(Role.EXPERT, testMember.getRole());
        verify(memberRepository).save(testMember);
        verify(expertProfileRepository).save(any(ExpertProfile.class));
        verify(expertProfileSpecialtyDetailRepository).save(any(ExpertProfileSpecialtyDetail.class));
        verify(skillRepository, times(2)).save(any(Skill.class));
        verify(careerRepository, times(1)).save(any(Career.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void upgradeToExpert_이미전문가이면예외() {
        // given
        testMember.changeRole(Role.EXPERT);
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> expertService.upgradeToExpert("test@example.com", requestDto));

        assertEquals("이미 전문가로 등록된 사용자입니다.", exception.getMessage());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void upgradeToExpert_없는회원이면예외() {
        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> expertService.upgradeToExpert("test@example.com", requestDto));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EXPERT"})
    void updateExpertProfile_정상작동() {
        // given
        testMember.changeRole(Role.EXPERT);

        // 기존 전문가 프로필 생성
        ExpertProfile existingProfile = ExpertProfile.createExpertProfile(
                testMember,
                "기존 소개", "기존 지역", 2, "기존 학력", 3,
                "oldUrl", "oldFb", "oldX", "oldInsta"
        );

        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testMember));
        when(expertProfileRepository.findByMember(testMember)).thenReturn(Optional.of(existingProfile));

        // Specialty 및 DetailField mock 설정
        Specialty dummySpecialty = new Specialty("디자인");
        when(specialtyRepository.findByName("디자인"))
                .thenReturn(Optional.of(dummySpecialty));

        when(detailFieldRepository.findByName("웹/모바일 디자인"))
                .thenReturn(Optional.of(new DetailField("웹/모바일 디자인", dummySpecialty)));

        when(detailFieldRepository.findByName("마케팅 디자인"))
                .thenReturn(Optional.of(new DetailField("마케팅 디자인", dummySpecialty)));

        // Skill mock 설정
        SkillCategory dummyCategory = new SkillCategory("IT/프로그래밍");
        Skill javaSkill = new Skill("Java", dummyCategory);
        Skill springSkill = new Skill("Spring", dummyCategory);

        when(skillRepository.findByNameAndCategory("Java", dummyCategory))
                .thenReturn(Optional.of(javaSkill));
        when(skillRepository.findByNameAndCategory("Spring", dummyCategory))
                .thenReturn(Optional.of(springSkill));
        when(skillCategoryRepository.findByName("IT/프로그래밍"))
                .thenReturn(Optional.of(dummyCategory));

        // when
        expertService.updateExpertProfile("test@example.com", requestDto);

        // then
        verify(expertProfileSpecialtyDetailRepository).deleteAllByExpertProfile(existingProfile);
        verify(careerRepository).deleteAllByExpertProfile(existingProfile);
        verify(expertProfileRepository, atLeastOnce()).save(existingProfile);

        assertEquals("소개글", existingProfile.getIntroduction());
        assertEquals("서울 강남구", existingProfile.getRegion());
        assertEquals(5, existingProfile.getTotalCareerYears());
        assertEquals("서울대", existingProfile.getEducation());
        assertEquals(10, existingProfile.getEmployeeCount());

        assertEquals(2, existingProfile.getSpecialtyDetailFields().size());
        assertEquals(2, existingProfile.getSkills().size());
        assertEquals(1, existingProfile.getCareers().size());
    }

    @Test
    void getPortfolioDetail_정상조회() {
        // given: 테스트용 ExpertProfile 객체 생성 및 필드 세팅
        ExpertProfile expertProfile = new ExpertProfile();
        try {
            // 리뷰 수 세팅 (private 필드이므로 리플렉션 사용)
            Field reviewCountField = ExpertProfile.class.getDeclaredField("reviewCount");
            reviewCountField.setAccessible(true);
            reviewCountField.set(expertProfile, 5L);

            // 평점 세팅
            Field ratingField = ExpertProfile.class.getDeclaredField("rating");
            ratingField.setAccessible(true);
            ratingField.set(expertProfile, 4.5);
        } catch (Exception e) {
            fail("리플렉션 세팅 실패");
        }

        // given: 테스트용 Portfolio 객체 생성 및 필드 세팅
        Portfolio portfolio = new Portfolio();
        try {
            Field portfolioIdField = Portfolio.class.getDeclaredField("portfolioId");
            portfolioIdField.setAccessible(true);
            portfolioIdField.set(portfolio, 100L);

            Field titleField = Portfolio.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(portfolio, "포트폴리오 제목");

            Field contentField = Portfolio.class.getDeclaredField("content");
            contentField.setAccessible(true);
            contentField.set(portfolio, "포트폴리오 내용");

            Field viewCountField = Portfolio.class.getDeclaredField("viewCount");
            viewCountField.setAccessible(true);
            viewCountField.set(portfolio, 123L);

            Field workingYearField = Portfolio.class.getDeclaredField("workingYear");
            workingYearField.setAccessible(true);
            workingYearField.set(portfolio, 3);

            Field categoryField = Portfolio.class.getDeclaredField("category");
            categoryField.setAccessible(true);
            categoryField.set(portfolio, "디자인");

            // Portfolio에 ExpertProfile 연결
            Field expertProfileField = Portfolio.class.getDeclaredField("expertProfile");
            expertProfileField.setAccessible(true);
            expertProfileField.set(portfolio, expertProfile);
        } catch (Exception e) {
            fail("리플렉션 세팅 실패");
        }

        // given: PortfolioImage 리스트 생성 및 필드 세팅
        PortfolioImage img1 = new PortfolioImage(portfolio, "http://image1.url", 1);
        PortfolioImage img2 = new PortfolioImage(portfolio, "http://image2.url", 2);
        try {
            // 이미지 ID 세팅 (리플렉션)
            Field imgIdField = PortfolioImage.class.getDeclaredField("portfolioImageId");
            imgIdField.setAccessible(true);
            imgIdField.set(img1, 10L);
            imgIdField.set(img2, 20L);
        } catch (Exception e) {
            fail("리플렉션 세팅 실패");
        }

        // 이미지 리스트를 Portfolio에 연결
        List<PortfolioImage> images = new ArrayList<>();
        images.add(img1);
        images.add(img2);

        try {
            Field imagesField = Portfolio.class.getDeclaredField("images");
            imagesField.setAccessible(true);
            imagesField.set(portfolio, images);
        } catch (Exception e) {
            fail("리플렉션 세팅 실패");
        }

        // Mock 설정: portfolioRepository.findById 호출 시 테스트용 portfolio 반환
        when(portfolioRepository.findById(100L)).thenReturn(Optional.of(portfolio));

        // when: 서비스 메서드 호출
        PortfolioDetailResponseDto dto = expertService.getPortfolioDetail(100L);

        // then: 반환된 DTO 검증
        assertNotNull(dto); // DTO가 null이 아님을 확인
        assertEquals(100L, dto.getPortfolioId());
        assertEquals("포트폴리오 제목", dto.getTitle());
        assertEquals("포트폴리오 내용", dto.getContent());
        assertEquals(123L, dto.getViewCount());
        assertEquals(3, dto.getWorkingYear());
        assertEquals("디자인", dto.getCategory());

        assertEquals(2, dto.getImages().size()); // 이미지 2개가 들어있음
        assertEquals(10L, dto.getImages().get(0).getId());
        assertEquals("http://image1.url", dto.getImages().get(0).getUrl());
        assertEquals(20L, dto.getImages().get(1).getId());
        assertEquals("http://image2.url", dto.getImages().get(1).getUrl());

        assertEquals(5L, dto.getReviewCount()); // 리뷰 수
        assertEquals(4.5, dto.getRating());     // 평점
    }

}
