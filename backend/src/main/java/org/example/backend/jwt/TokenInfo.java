package org.example.backend.jwt;

public class TokenInfo {
    private final String token;
    private final String jti;
    private final long expirationMs;

    public TokenInfo(String token, String jti, long expirationMs) {
        this.token = token;
        this.jti = jti;
        this.expirationMs = expirationMs;
    }

    public String getToken() {
        return token;
    }

    public String getJti() {
        return jti;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
