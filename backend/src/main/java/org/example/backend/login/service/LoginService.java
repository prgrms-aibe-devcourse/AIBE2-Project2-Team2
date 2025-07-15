package org.example.backend.login.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.User;
import org.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;

    @Transactional
    public void updateLastLogin(String email) {
        log.info("updateLastLogin 시작 - email: {}", email);

        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            log.info("User 조회 성공 - userId: {}", user.getUserId());
            user.updateLastLoginAt(LocalDateTime.now());
            log.info("lastLoginAt 필드 변경 완료");
        }, () -> {
            log.warn("User 정보 없음 - email: {}", email);
        });

        log.info("updateLastLogin 종료 (트랜잭션 커밋 대기 중일 수 있음)");
    }

    public User findByEmail(String email) {
        log.info("findByEmail 시작 - email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
