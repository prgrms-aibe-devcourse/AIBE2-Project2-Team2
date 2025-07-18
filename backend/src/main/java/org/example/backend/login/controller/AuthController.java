package org.example.backend.login.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.login.dto.KakaoLoginResponseDto;
import org.example.backend.login.dto.SignupRequestDto;
import org.example.backend.login.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API") // ✅ Swagger 그룹 이름
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * 이메일, 비밀번호, 닉네임, 전화번호를 통해 회원가입을 진행합니다.
     *
     * @param signupRequestDto 회원가입 요청 DTO
     * @return 회원가입 성공 또는 실패 메시지
     */
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 전화번호를 통해 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"회원가입이 완료되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"이미 존재하는 이메일입니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"이메일 형식이 유효하지 않습니다.\"")
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto
    ) {
        log.info("회원가입 요청");
        boolean result = authService.signup(signupRequestDto);

        if (result) {
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
        }
    }

    /**
     * 로그아웃 요청 처리
     * 현재 로그인된 사용자를 로그아웃 처리합니다.
     *
     * @param response HTTP 응답 객체
     * @param principal 인증된 사용자 정보 (스프링 시큐리티)
     * @return 로그아웃 결과 메시지
     * @throws IOException 응답 쓰기 실패 시
     */
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(value = "{\"message\": \"로그아웃 성공\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - JWT 토큰이 없거나 유효하지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized: Invalid or expired JWT token\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"사용자를 찾을 수 없습니다.\"")
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response,
            Principal principal
    ) throws IOException {
        String email = principal.getName();
        log.info("로그아웃 요청 - email: {}", email);
        authService.logout(email, response);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "카카오 로그인 처리",
            description = "카카오 인가 코드를 받아 엑세스 토큰을 발급받고 사용자 정보를 조회하여 자동 회원가입 및 로그인 처리합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 또는 자동 회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KakaoLoginResponseDto.class),
                            examples = @ExampleObject(
                                    name = "성공 응답",
                                    value = "{\n" +
                                            "  \"message\": \"로그인 성공\",\n" +
                                            "  \"nickname\": \"홍길동\",\n" +
                                            "  \"role\": \"USER\"\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (인가 코드 누락 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"인가 코드가 누락되었거나 유효하지 않습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "카카오 API 통신 실패 또는 서버 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"카카오 서버와의 통신에 실패했습니다.\"")
                    )
            )
    })
    @GetMapping("/kakao/callback")
    public ResponseEntity<?> kakaoCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) {
        log.info("카카오 로그인 콜백 요청 code: {}", code);
        KakaoLoginResponseDto result = authService.kakaoLogin(code, response);
        return ResponseEntity.ok(result);
    }
}
