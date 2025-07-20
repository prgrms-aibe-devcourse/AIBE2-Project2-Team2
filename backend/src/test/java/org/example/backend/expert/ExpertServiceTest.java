package org.example.backend.expert;

import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.expert.dto.request.ExpertRequestDto;
import org.example.backend.expert.dto.request.SkillDto;
import org.example.backend.expert.dto.request.SpecialtyDetailRequestDto;
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
}
