package org.example.backend.jwt;

import lombok.RequiredArgsConstructor;
import org.example.backend.redis.RedisService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisService redisService;

    public void blacklistAllActiveTokens(String email) {
        Set<String> activeJtis = redisService.getUserActiveTokens(email);
        if (activeJtis == null || activeJtis.isEmpty()) {
            return;
        }

        for (String jti : activeJtis) {
            Long expirationMillis = redisService.getExpirationMillis("jti:" + jti);
            if (expirationMillis == null || expirationMillis <= 0) {
                expirationMillis = 3600 * 1000L; // 기본 1시간
            }

            redisService.deleteActiveToken(jti);
            redisService.blacklistToken(jti, expirationMillis);
        }

        redisService.deleteUserActiveTokens(email); // 최종 정리
    }
}
