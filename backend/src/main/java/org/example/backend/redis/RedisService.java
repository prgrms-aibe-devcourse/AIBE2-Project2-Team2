package org.example.backend.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    // 활성 토큰 저장 및 이메일-토큰 관계 저장 (Set에 추가)
    public void storeActiveToken(String jti, String email, long expirationMillis) {
        try {
            // JTI 키에 이메일 저장 및 만료 시간 설정
            redisTemplate.opsForValue().set("jti:" + jti, email, expirationMillis, TimeUnit.MILLISECONDS);

            // 사용자 이메일 기준 활성 JTI 집합에 추가
            redisTemplate.opsForSet().add("activeTokens:" + email, jti);

            // 활성 JTI 집합 만료 시간 갱신 (토큰 만료 시간과 동기화)
            redisTemplate.expire("activeTokens:" + email, expirationMillis, TimeUnit.MILLISECONDS);

            // 로그 출력
            log.info("[Redis] 활성 토큰 저장 완료, jti: {}, email: {}, 만료 시간: {}ms", jti, email, expirationMillis);

        } catch (Exception e) {
            log.error("[Redis] 활성 토큰 저장 중 오류 발생, jti: {}, email: {}", jti, email, e);
            throw e; // 필요시 예외 재던지기 (또는 별도 처리)
        }
    }


    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey("jti:" + jti + ":blacklist");
    }

    public void blacklistToken(String jti, long expirationMillis) {
        redisTemplate.opsForValue().set("jti:" + jti + ":blacklist", "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }

    // 이메일 기준 활성 토큰(JTI) 목록 조회
    public Set<String> getUserActiveTokens(String email) {
        return redisTemplate.opsForSet().members("activeTokens:" + email);
    }

    // 특정 JTI 활성 토큰 삭제
    public void deleteActiveToken(String jti) {
        redisTemplate.delete("jti:" + jti);
    }

    // 이메일 기준 활성 토큰 목록 삭제
    public void deleteUserActiveTokens(String email) {
        redisTemplate.delete("activeTokens:" + email);
    }

    // 특정 키의 만료 시간 조회 (밀리초 단위)
    public long getExpirationMillis(String key) {
        Long expireSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expireSeconds == null || expireSeconds < 0) {
            return 0L; // 또는 적절한 기본값
        }
        return expireSeconds * 1000;
    }

}

