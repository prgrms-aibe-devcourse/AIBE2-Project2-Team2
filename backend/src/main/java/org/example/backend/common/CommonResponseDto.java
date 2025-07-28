package org.example.backend.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
@Schema(description = "토큰 검사 후 내 정보 반환 DTO")
public class CommonResponseDto {

    @Schema(description = "사용자 이름", example = "홍길동")
    private String nickname;

    @Schema(description = "사용자 이메일", example = "example123@naver.com")
    private String email;

    @Schema(description = "권한", example = "USER")
    private String role;

    @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

}
