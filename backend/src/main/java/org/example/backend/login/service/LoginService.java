package org.example.backend.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Member;
import org.example.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updateLastLogin(String email) {
        log.info("updateLastLogin 시작 - email: {}", email);

        memberRepository.findByEmail(email).ifPresentOrElse(user -> {
            log.info("Member 조회 성공 - memberId: {}", user.getMemberId());
            user.updateLastLoginAt(LocalDateTime.now());
            log.info("lastLoginAt 필드 변경 완료");
        }, () -> {
            log.warn("Member 정보 없음 - email: {}", email);
        });

        log.info("updateLastLogin 종료 (트랜잭션 커밋 대기 중일 수 있음)");
    }

    public Member findByEmail(String email) {
        log.info("findByEmail 시작 - email: {}", email);
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
