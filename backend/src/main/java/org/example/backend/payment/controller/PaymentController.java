package org.example.backend.payment.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.payment.service.KakaoPayService;
import org.example.backend.repository.PaymentRepository;
import org.example.backend.repository.MemberRepository;
import org.example.backend.entity.Payment;
import org.example.backend.entity.Member;
import org.example.backend.payment.dto.KakaoPayReadyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final KakaoPayService kakaoPayService;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;

    // 결제 준비 (카카오페이 결제창 URL 반환)
    @PostMapping("/kakao/ready")
    public ResponseEntity<KakaoPayReadyResponse> kakaoPayReady(@RequestParam Long matchingId, @RequestParam String userId) {
        KakaoPayReadyResponse response = kakaoPayService.kakaoPayReady(matchingId, userId);
        return ResponseEntity.ok(response);
    }

    // 결제 승인 콜백
    @GetMapping("/kakao/success")
    public ResponseEntity<String> kakaoPaySuccess(@RequestParam String pg_token, @RequestParam Long matchingId) {
        String response = kakaoPayService.kakaoPayApprove(pg_token, matchingId);
        return ResponseEntity.ok(response);
    }

    // 결제 취소 (카카오페이 결제 취소 API 연동)
    @PostMapping("/kakao/cancel")
    public ResponseEntity<String> kakaoPayCancel(@RequestParam Long matchingId, @RequestParam(required = false) String reason) {
        String response = kakaoPayService.kakaoPayCancel(matchingId, reason);
        return ResponseEntity.ok(response);
    }

    // 결제 내역 조회 (회원별)
    @GetMapping("/history/{memberId}")
    public ResponseEntity<List<Payment>> getPaymentHistory(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        List<Payment> payments = paymentRepository.findAllByMatching_Member(member);
        return ResponseEntity.ok(payments);
    }
} 