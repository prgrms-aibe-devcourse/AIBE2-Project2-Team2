package org.example.backend.common;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/common")
@Tag(name = "Common", description = "내정보 체크 및 유효성 조회 API")
public class CommonController {

    private final CommonService commonService;

    /**
     * 토큰 검증 후 이메일로 회원 정보 조회
     */
    @Operation(
            summary = "회원 정보 확인",
            description = "JWT 토큰을 검증한 후, 인증된 사용자의 이메일을 기반으로 회원 정보를 조회한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (유효하지 않은 토큰)",
                    content = @Content
            )
    })
    @GetMapping("/check")
    public CommonResponseDto checkUserInfo(
            Principal principal
    ) {
        String email = principal.getName();
        return commonService.checkUserInfo(email);
    }
}
