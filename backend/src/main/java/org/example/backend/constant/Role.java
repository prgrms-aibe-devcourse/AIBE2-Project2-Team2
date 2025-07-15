package org.example.backend.constant;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public enum Role {
    USER, // 일반 사용자
    EXPERT, // 전문가
    ADMIN; // 관리자

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
