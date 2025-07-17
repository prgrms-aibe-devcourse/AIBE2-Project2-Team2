package org.example.backend.expert;

import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.expert.dto.ExpertRequestDto;
import org.example.backend.expert.dto.SpecialtyDetailRequestDto;
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
                new SpecialtyDetailRequestDto("디자인", List.of("UX/UI 디자인", "그래픽 디자인"))
                // 필요하면 다른 전문분야 추가 가능
        );

        requestDto = new ExpertRequestDto(
                specialtyDetailList,    // 변경됨
                "소개글",
                "서울 강남구",
                5,
                "서울대",
                10,
                "https://site.com",
                "https://facebook.com",
                "https://x.com",
                "https://instagram.com",
                List.of("Java", "Spring"),
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

        when(detailFieldRepository.findByName("UX/UI 디자인"))
                .thenReturn(Optional.of(new DetailField("UX/UI 디자인", dummySpecialty)));

        when(detailFieldRepository.findByName("그래픽 디자인"))
                .thenReturn(Optional.of(new DetailField("그래픽 디자인", dummySpecialty)));

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
}
