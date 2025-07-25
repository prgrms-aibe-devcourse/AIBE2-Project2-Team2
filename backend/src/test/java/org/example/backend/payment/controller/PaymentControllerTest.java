package org.example.backend.payment.controller;

import org.example.backend.entity.Member;
import org.example.backend.entity.Matching;
import org.example.backend.entity.Payment;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private MatchingRepository matchingRepository;

    @MockBean
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("결제 준비 API 테스트")
    void testKakaoPayReady() throws Exception {
        // given
        Long matchingId = 1L;
        String userId = "testuser";
        Matching matching = Mockito.mock(Matching.class);
        Mockito.when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        // 실제 서비스에서는 KakaoPayService도 Mocking 필요 (여기선 간단히 통과만 확인)
        mockMvc.perform(post("/api/payment/kakao/ready")
                        .param("matchingId", matchingId.toString())
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결제 승인 콜백 API 테스트")
    void testKakaoPaySuccess() throws Exception {
        Long matchingId = 1L;
        String pgToken = "test_pg_token";
        Matching matching = Mockito.mock(Matching.class);
        Mockito.when(matchingRepository.findById(matchingId)).thenReturn(Optional.of(matching));

        mockMvc.perform(get("/api/payment/kakao/success")
                        .param("pg_token", pgToken)
                        .param("matchingId", matchingId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결제 내역 조회 API 테스트")
    void testGetPaymentHistory() throws Exception {
        Long memberId = 1L;
        Member member = Mockito.mock(Member.class);
        Mockito.when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        Mockito.when(paymentRepository.findAllByMatching_Member(member))
                .thenReturn(Collections.singletonList(Mockito.mock(Payment.class)));

        mockMvc.perform(get("/api/payment/history/{memberId}", memberId))
                .andExpect(status().isOk());
    }
}