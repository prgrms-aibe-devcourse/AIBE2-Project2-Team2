package org.example.backend.login.dto;

import lombok.Data;

@Data
public class KakaoLoginResponseDto {
    private String message;
    private String nickname;
    private String role;
}
