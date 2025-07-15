package org.example.backend.openFeign;

import lombok.Data;

@Data
public class KakaoUserResponse {
    private Long id;
    private KakaoAccount kakao_account;

    @Data
    public static class KakaoAccount {
        private String email;
        private String name;
        private String phone_number;
    }
}
