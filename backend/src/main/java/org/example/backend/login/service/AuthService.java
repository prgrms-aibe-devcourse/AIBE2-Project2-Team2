package org.example.backend.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.JoinType;
import org.example.backend.entity.Member;
import org.example.backend.exception.customException.UserNotFoundException;
import org.example.backend.jwt.JwtUtil;
import org.example.backend.jwt.TokenBlacklistService;
import org.example.backend.jwt.TokenInfo;
import org.example.backend.login.dto.KakaoLoginResponseDto;
import org.example.backend.login.dto.SignupRequestDto;
import org.example.backend.openFeign.KakaoApiClient;
import org.example.backend.openFeign.KakaoAuthClient;
import org.example.backend.openFeign.KakaoTokenResponse;
import org.example.backend.openFeign.KakaoUserResponse;
import org.example.backend.redis.RedisService;
import org.example.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final JwtUtil jwtUtil;
    private final RedisService redisService;
    private final LoginService loginService;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    // 일반 가입
    public boolean signup(SignupRequestDto signupRequestDto) {
        // 1. 이메일 중복 확인
        log.info("회원 가입 요청: {}", signupRequestDto);
        if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {
            log.info("이미 존재하는 이메일: {}", signupRequestDto.getEmail());
            return false; // 이메일이 이미 존재하면 false 반환
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        log.info("암호화된 비밀번호 생성 완료 : {}", encodedPassword);

        // 3. 정적 팩토리 메서드로 User 생성
        Member member = Member.create(
                signupRequestDto.getEmail(),
                encodedPassword,
                signupRequestDto.getNickname(),
                signupRequestDto.getPhone(),
                signupRequestDto.getJoinType()
        );

        // 4. User 저장
        memberRepository.save(member);
        log.info("회원 가입 완료: {}", member.getEmail());
        return true; // 회원 가입 성공 시 true 반환
    }

    // 로그아웃 처리 메소드
    public void logout(String email, HttpServletResponse response) throws IOException {

        // 사용자 존재 여부 확인
        validateUserExists(email);
        log.info("로그아웃 요청 - 이메일: {}", email);

        // 활성 통큰 블랙리스트 처리
        tokenBlacklistService.blacklistAllActiveTokens(email);

        // 쿠키 삭제 처리 (Cookie 객체 생성 방식)
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);            // HTTPS 환경이라면 true, 개발 환경에서는 false로 조절
        cookie.setPath("/");
        cookie.setMaxAge(0);               // 즉시 만료

        response.addCookie(cookie);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"로그아웃 성공\"}");
    }

    // 사용자 조회 메서드 분리
    private void validateUserExists(String email) {
        memberRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("로그아웃 실패 - 사용자 없음: {}", email);
            return new UserNotFoundException("사용자를 찾을 수 없습니다.");
        });
    }

    // 카카오 로그인 처리 메소드
    public KakaoLoginResponseDto kakaoLogin(String code, HttpServletResponse response) {
        // 0. 결과 변수 초기화
        KakaoLoginResponseDto result = new KakaoLoginResponseDto();

        // 1. access_token 요청
        KakaoTokenResponse tokenResponse = kakaoAuthClient.getToken(
                "authorization_code",
                kakaoClientId,
                kakaoRedirectUri,
                code
        );

        String accessToken = tokenResponse.getAccess_token();

        // 2. 사용자 정보 요청
        KakaoUserResponse userInfo = kakaoApiClient.getUserInfo("Bearer " + accessToken);

        String email = userInfo.getKakao_account().getEmail();
        String nickname = userInfo.getKakao_account().getName();
        String phone = userInfo.getKakao_account().getPhone_number();

        // 3. DB 조회 및 분기 처리
        Optional<Member> userOpt = memberRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            // 로그인 처리
            loginAndSetCookie(userOpt.get(), response, result);
            return result;
        } else {
            // 자동 회원가입
            Member newMember = Member.create(
                    email,
                    UUID.randomUUID().toString(), // 패스워드는 임의 UUID → 사용자는 직접 로그인 못함
                    nickname,
                    phone,
                    JoinType.KAKAO
            );
            memberRepository.save(newMember);
            loginAndSetCookie(newMember, response, result);
            return result;
        }
    }

    private void loginAndSetCookie(Member member, HttpServletResponse response, KakaoLoginResponseDto dto) {
        TokenInfo tokenInfo = jwtUtil.createToken(member.getEmail(), String.valueOf(member.getRole()));
        redisService.storeActiveToken(tokenInfo.getJti(), member.getEmail(), tokenInfo.getExpirationMs());
        loginService.updateLastLogin(member.getEmail());

        Cookie cookie = new Cookie("token", tokenInfo.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int)(tokenInfo.getExpirationMs() / 1000));
        response.addCookie(cookie);

        dto.setMessage("로그인 성공");
        dto.setNickname(member.getNickname());
        dto.setRole(member.getRole().toString());
        dto.setProfileImageUrl(member.getProfileImageUrl());
        dto.setEmail(member.getEmail());
    }

    public void issueNewTokenAndSetCookie(Member member, HttpServletResponse response) {
        String email = member.getEmail();

        // ✅ 1. 기존 토큰 블랙리스트 처리
        tokenBlacklistService.blacklistAllActiveTokens(email);

        // ✅ 2. 새 토큰 발급
        TokenInfo tokenInfo = jwtUtil.createToken(email, member.getRole().name());

        // ✅ 3. Redis에 jti 저장
        redisService.storeActiveToken(tokenInfo.getJti(), email, tokenInfo.getExpirationMs());

        // ✅ 4. 쿠키로 내려주기
        Cookie cookie = new Cookie("token", tokenInfo.getToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // 운영에서는 true
        cookie.setPath("/");
        cookie.setMaxAge((int) (tokenInfo.getExpirationMs() / 1000));

        response.addCookie(cookie);
    }
}
