package org.example.backend.mypage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backend.constant.JoinType;

@Getter
@AllArgsConstructor
@Schema(description = "마이페이지 내 정보 응답 DTO")
public class MyPageResponseDto {

    @Schema(description = "닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "가입 방식 (자체, KAKAO 등)", example = "KAKAO")
    private JoinType joinType;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
}
