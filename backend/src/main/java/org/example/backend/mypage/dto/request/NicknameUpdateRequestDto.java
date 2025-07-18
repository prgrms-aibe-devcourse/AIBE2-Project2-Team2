package org.example.backend.mypage.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
public class NicknameUpdateRequestDto {

    @Schema(description = "닉네임 (한글, 영어, 숫자만 가능, 최소 2글자)", example = "홍길동")
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영어, 숫자만 사용할 수 있습니다.")
    @Size(min = 2, message = "닉네임은 최소 2글자 이상이어야 합니다.")
    private String nickname;
}
