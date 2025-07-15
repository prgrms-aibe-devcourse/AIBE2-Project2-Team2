package org.example.backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 생성
    public TokenInfo createToken(String email, String role) {
        Date now = new Date();
        long expirationMs = 6 * 60 * 60 * 1000;
        Date expiryDate = new Date(now.getTime() + expirationMs);
        String jti = UUID.randomUUID().toString(); // JTI 생성

        String token = Jwts.builder()
                .subject(email)
                .id(jti)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        return new TokenInfo(token, jti, expirationMs);
    }


    // 공통 Claims 추출
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getJti(String token) {
        return extractClaims(token).getId();
    }

    // 사용자 이름 = 이메일 추출
    public String getUsername(String token) {
        return extractClaims(token).getSubject();
    }


    // 역할(role) 추출
    public String getRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // 전체 유효성 검사 (서명 + 만료)  >>> 로직 추가 예정
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);

            // 만료 여부
            if (claims.getExpiration().before(new Date())) {
                log.warn("JWT 만료됨");
                return false;
            }

            // 필수 클레임 검사
            if (claims.getSubject() == null || claims.get("role") == null || claims.getId() == null) {
                log.warn("JWT 클레임 누락");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("JWT 유효성 검사 실패", e);
            return false;
        }
    }

}
