package org.example.backend.mypage;

import org.example.backend.constant.JoinType;
import org.example.backend.entity.Member;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.mypage.dto.request.NicknameUpdateRequestDto;
import org.example.backend.mypage.dto.response.MyPageResponseDto;
import org.example.backend.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MyPageServiceTest {

    @Autowired
    private MyPageService myPageService;

    @MockBean
    private MemberRepository memberRepository;

    private MyPageResponseDto responseDto;

    @BeforeEach
    void setup() {
        responseDto = new MyPageResponseDto(
                "홍길동",
                "https://cdn.example.com/profile.jpg",
                "test@example.com",
                JoinType.KAKAO
        );
    }

    @Test
    void getMyPageInfo_성공() {
        String email = "test@example.com";

        when(memberRepository.findMyPageInfoByEmail(email)).thenReturn(responseDto);

        MyPageResponseDto result = myPageService.getMyPageInfo(email);

        assertNotNull(result);
        assertEquals("홍길동", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(JoinType.KAKAO, result.getJoinType());

        verify(memberRepository, times(1)).findMyPageInfoByEmail(email);
    }

    @Test
    void getMyPageInfo_회원없음_예외발생() {
        String email = "notfound@example.com";

        when(memberRepository.findMyPageInfoByEmail(email)).thenReturn(null);

        MemberNotFoundException ex = assertThrows(MemberNotFoundException.class,
                () -> myPageService.getMyPageInfo(email));

        assertEquals("해당 이메일의 사용자가 존재하지 않습니다.", ex.getMessage());
        verify(memberRepository, times(1)).findMyPageInfoByEmail(email);
    }
    @Test
    void updateNickname_성공() {
        String email = "test@example.com";
        String newNickname = "새닉네임";

        // Member 생성
        Member member = Member.create(email, "encodedPassword", "기존닉네임", "01012345678", JoinType.KAKAO);

        NicknameUpdateRequestDto dto = createDtoWithNickname(newNickname);

        when(memberRepository.findByEmail(email)).thenReturn(java.util.Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        myPageService.updateNickname(email, dto);

        assertEquals(newNickname, member.getNickname());
        verify(memberRepository).findByEmail(email);
        verify(memberRepository).save(member);
    }

    private NicknameUpdateRequestDto createDtoWithNickname(String nickname) {
        // DTO에 세터가 없으면 생성자나 리플렉션으로 세팅
        try {
            NicknameUpdateRequestDto dto = NicknameUpdateRequestDto.class.getDeclaredConstructor().newInstance();
            java.lang.reflect.Field field = NicknameUpdateRequestDto.class.getDeclaredField("nickname");
            field.setAccessible(true);
            field.set(dto, nickname);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
