package org.example.backend.mypage;

import org.example.backend.constant.JoinType;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.entity.Content;
import org.example.backend.entity.Matching;
import org.example.backend.entity.Member;
import org.example.backend.entity.Payment;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.mypage.dto.request.NicknameUpdateRequestDto;
import org.example.backend.mypage.dto.response.MyPageResponseDto;
import org.example.backend.mypage.dto.response.PaymentResponseDto;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.PaymentRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MyPageServiceTest {

    @Autowired
    private MyPageService myPageService;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    private MyPageResponseDto responseDto;

    @BeforeEach
    void setup() {
        responseDto = new MyPageResponseDto(
                "홍길동",
                "https://cdn.example.com/profile.jpg",
                "test@example.com",
                JoinType.KAKAO,
                "010-1234-5678"
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

    @Test
    void getUserPayments_전체조회_정상동작() {
        // Given
        String email = "test@example.com";
        Member member = Member.create(email, "encoded", "홍길동", "01012345678", JoinType.NORMAL);
        Content content = createContentWithMember(member);
        Matching matching = createMatching(member, content);

        Payment payment1 = createPayment(1L, 10000L, PaymentStatus.PAID, matching);
        Payment payment2 = createPayment(2L, 20000L, PaymentStatus.CANCELLED, matching);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(paymentRepository.findAllByMatching_Member(member)).thenReturn(List.of(payment1, payment2));

        // When
        List<PaymentResponseDto> result = myPageService.getUserPayments(email, null);

        // Then
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getPaymentId()); // 최신순
        assertEquals(1L, result.get(1).getPaymentId());
    }
    private Payment createPayment(Long id, Long cost, PaymentStatus status, Matching matching) {
        Payment payment = mock(Payment.class);
        when(payment.getPaymentId()).thenReturn(id);
        when(payment.getCost()).thenReturn(cost);
        when(payment.getStatus()).thenReturn(status);
        when(payment.getUpdateTime()).thenReturn(LocalDateTime.now().plusSeconds(id)); // ID 2가 더 최신
        when(payment.getRegTime()).thenReturn(LocalDateTime.now().minusDays(1));
        when(payment.getMatching()).thenReturn(matching);
        return payment;
    }

    private Matching createMatching(Member mentee, Content content) {
        Matching matching = mock(Matching.class);
        when(matching.getContent()).thenReturn(content);
        when(matching.getMember()).thenReturn(mentee);
        return matching;
    }

    private Content createContentWithMember(Member expert) {
        Content content = mock(Content.class);
        when(content.getTitle()).thenReturn("테스트 컨텐츠");
        when(content.getContentId()).thenReturn(101L);
        when(content.getMember()).thenReturn(expert);
        return content;
    }

    @Test
    void getUserPayments_결제상태필터_정상동작() {
        // Given
        String email = "test@example.com";
        Member member = Member.create(email, "encoded", "홍길동", "01012345678", JoinType.NORMAL);
        Content content = createContentWithMember(member);
        Matching matching = createMatching(member, content);

        Payment payment1 = createPayment(1L, 10000L, PaymentStatus.PAID, matching);
        Payment payment2 = createPayment(2L, 20000L, PaymentStatus.CANCELLED, matching);
        Payment payment3 = createPayment(3L, 30000L, PaymentStatus.PAID, matching);

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(paymentRepository.findAllByMatching_Member(member)).thenReturn(List.of(payment1, payment2, payment3));

        // When: PAID 상태만 필터링
        List<PaymentResponseDto> result = myPageService.getUserPayments(email, PaymentStatus.PAID);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getStatus().equals(PaymentStatus.PAID.name())));
        assertEquals(3L, result.get(0).getPaymentId()); // 최신순 정렬 확인
        assertEquals(1L, result.get(1).getPaymentId());
    }
}
