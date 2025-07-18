package org.example.backend.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.constant.JoinType;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.*;

@Data
@AllArgsConstructor
public class SignupRequestDto {

    @NotBlank
    @Schema(description = "사용자 닉네임", example = "user123")
    private String nickname;    // 사용자 이름

    @NotBlank
    @Schema(description = "비밀번호", example = "Password@123")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,20}$",
            message = "비밀번호는 영문(A-z 대소문자 구분), 숫자(0-9), 특수문자(!@#$%^&*)를 모두 포함해야 합니다."
    )
    private String password;    // 비밀번호

    @NotBlank
    @Pattern(
            regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
    )
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;         // 전화번호

    @NotBlank
    @Email
    @Schema(description = "이메일", example = "example@naver.com")
    private String email;       // 이메일

    @NotNull
    @Schema(description = "회원가입 타입 (예: KAKAO, NORMAL)", example = "NORMAL")
    private JoinType joinType;    // 회원가입 타입 (예: KAKAO, NORMAL 등)

}
