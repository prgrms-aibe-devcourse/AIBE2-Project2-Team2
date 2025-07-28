package org.example.backend.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper; // 추가!
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.example.backend.entity.EstimateRecord;
import org.example.backend.repository.EstimateRecordRepository;
import lombok.RequiredArgsConstructor;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.PaymentRepository;
import org.example.backend.entity.Payment;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.entity.Matching;
import org.example.backend.payment.dto.KakaoPayReadyRequest;
import org.example.backend.payment.dto.KakaoPayReadyResponse;
import org.example.backend.payment.dto.KakaoPayApproveRequest;
import org.example.backend.payment.dto.KakaoPayCancelRequest;
import org.example.backend.matching.service.MatchingService;

@Service
@RequiredArgsConstructor
public class KakaoPayService {
    private final EstimateRecordRepository estimateRecordRepository;
    private final MatchingRepository matchingRepository;
    private final PaymentRepository paymentRepository;
    private final MatchingService matchingService;

    @Value("${kakao.pay.secret-key}")
    private String kakaopaySecretKey;

    @Value("${kakao.pay.cid}")
    private String cid;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. 결제 준비(ready)
    public KakaoPayReadyResponse kakaoPayReady(Long matchingId, String userId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매칭이 존재하지 않습니다."));
        EstimateRecord estimate = estimateRecordRepository.findByMatching(matching)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "견적이 존재하지 않습니다."));

        KakaoPayReadyRequest requestDto = new KakaoPayReadyRequest();
        requestDto.setCid(cid);
        requestDto.setPartner_order_id(matchingId.toString());
        requestDto.setPartner_user_id(userId);
        requestDto.setItem_name("컨텐츠 결제");
        requestDto.setQuantity("1");
        requestDto.setTotal_amount(estimate.getTotalPrice().toString());
        requestDto.setTax_free_amount("0");
        requestDto.setApproval_url("http://localhost:5173/mypage/matching/history?matchingId=" + matchingId);
        requestDto.setCancel_url("http://localhost:5173/mypage/matching/history");
        requestDto.setFail_url("http://localhost:5173/mypage/matching/history");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "DEV_SECRET_KEY " + kakaopaySecretKey);

        // 요청 로그 출력
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("==== [카카오페이 ready 요청] ====");
            System.out.println("요청 URL : https://open-api.kakaopay.com/online/v1/payment/ready");
            System.out.println("요청 헤더 : " + headers.toString());
            System.out.println("cid: " + cid);
            System.out.println("secret-key: " + kakaopaySecretKey);
            System.out.println("requestDto: " + objectMapper.writeValueAsString(requestDto));
            System.out.println("===============================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        org.springframework.http.HttpEntity<KakaoPayReadyRequest> request = new org.springframework.http.HttpEntity<>(requestDto, headers);
        KakaoPayReadyResponse response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/ready",
                request,
                KakaoPayReadyResponse.class
        );

        // 응답 로그 출력
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("==== [카카오페이 ready 응답] ====");
            System.out.println(objectMapper.writeValueAsString(response));
            System.out.println("===============================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Payment 엔티티 생성 (결제 전 상태, tid 저장)
        Payment payment = new Payment();
        payment.setCost(estimate.getTotalPrice());
        payment.setStatus(PaymentStatus.NOT_PAID);
        payment.setMatching(matching);
        payment.setTid(response.getTid());
        paymentRepository.save(payment);

        return response;
    }

    // 2. 결제 승인(approve)
    public String kakaoPayApprove(String pgToken, Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매칭이 존재하지 않습니다."));
        EstimateRecord estimate = estimateRecordRepository.findByMatching(matching)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "견적이 존재하지 않습니다."));
        Payment payment = matching.getPayments().stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제 내역이 존재하지 않습니다."));

        KakaoPayApproveRequest requestDto = new KakaoPayApproveRequest();
        requestDto.setCid(cid);
        requestDto.setTid(payment.getTid());
        requestDto.setPartner_order_id(matchingId.toString());
        requestDto.setPartner_user_id("user"); // 실제 유저 ID 넣기
        requestDto.setPg_token(pgToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "SECRET_KEY " + kakaopaySecretKey);

        // 요청 로그 출력
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println("==== [카카오페이 approve 요청] ====");
            System.out.println("요청 URL : https://open-api.kakaopay.com/online/v1/payment/approve");
            System.out.println("요청 헤더 : " + headers.toString());
            System.out.println("cid: " + cid);
            System.out.println("secret-key: " + kakaopaySecretKey);
            System.out.println("requestDto: " + objectMapper.writeValueAsString(requestDto));
            System.out.println("===============================");
        } catch (Exception e) {
            e.printStackTrace();
        }

        org.springframework.http.HttpEntity<KakaoPayApproveRequest> request = new org.springframework.http.HttpEntity<>(requestDto, headers);
        String response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/approve",
                request,
                String.class
        );

        // 응답 로그 출력
        System.out.println("==== [카카오페이 approve 응답] ====");
        System.out.println(response);
        System.out.println("===============================");

        // 결제 승인 성공 시 상태 변경
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        // 매칭 상태도 ACCEPTED로 변경
        matchingService.updateMatchingStatusByPaymentResult(matchingId, PaymentStatus.PAID);
        return response;
    }

    // 3. 결제 취소(cancel)
    public String kakaoPayCancel(Long matchingId, String reason) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "매칭이 존재하지 않습니다."));
        Payment payment = matching.getPayments().stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제 내역이 존재하지 않습니다."));

        KakaoPayCancelRequest requestDto = new KakaoPayCancelRequest();
        requestDto.setCid(cid);
        requestDto.setTid(payment.getTid());
        requestDto.setCancel_amount(payment.getCost().intValue());
        requestDto.setCancel_tax_free_amount(0); // 필요시 세팅
        requestDto.setCancel_vat_amount(0); // 필요시 세팅
        requestDto.setCancel_reason(reason != null ? reason : "사용자 요청");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "SECRET_KEY " + kakaopaySecretKey);

        // 요청 로그
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            System.out.println("==== [카카오페이 cancel 요청] ====");
            System.out.println("요청 URL : https://open-api.kakaopay.com/online/v1/payment/cancel");
            System.out.println("요청 헤더 : " + headers.toString());
            System.out.println("cid: " + cid);
            System.out.println("tid: " + payment.getTid());
            System.out.println("requestDto: " + objectMapper.writeValueAsString(requestDto));
            System.out.println("===============================");
        } catch (Exception e) { e.printStackTrace(); }

        org.springframework.http.HttpEntity<KakaoPayCancelRequest> request = new org.springframework.http.HttpEntity<>(requestDto, headers);
        String response = restTemplate.postForObject(
                "https://open-api.kakaopay.com/online/v1/payment/cancel",
                request,
                String.class
        );

        // 응답 로그
        System.out.println("==== [카카오페이 cancel 응답] ====");
        System.out.println(response);
        System.out.println("===============================");

        // 결제 취소 성공 시 상태 변경
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        matching.setStatus(org.example.backend.constant.MatchingStatus.CANCELLED);
        matchingRepository.save(matching);
        return response;
    }
}
