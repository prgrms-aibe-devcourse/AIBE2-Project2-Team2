package org.example.backend.mypage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.PaymentStatus;
import org.example.backend.exception.customException.InvalidPaymentStatusException;
import org.example.backend.mypage.dto.request.NicknameUpdateRequestDto;
import org.example.backend.mypage.dto.response.MyPageResponseDto;
import org.example.backend.mypage.dto.response.PaymentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    /*
    * 마이페이지 정보를 조회하는 API
     */

    /**
     * 내 정보 조회
     * GET /api/me
     */
    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MyPageResponseDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"nickname\": \"홍길동\",\n" +
                                    "  \"profileImageUrl\": \"https://cdn.example.com/profile.jpg\",\n" +
                                    "  \"email\": \"hong@example.com\",\n" +
                                    "  \"joinType\": \"KAKAO\",\n" +
                                    "  \"phone\": \"010-1234-5678\"\n" +
                                    "}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"해당 이메일의 사용자가 존재하지 않습니다.\"")
                    )
            )
    })
    @GetMapping
    public ResponseEntity<MyPageResponseDto> getMyPageInfo(
            Principal principal
    ) {
        String email = principal.getName();

        log.info("마이페이지 정보 요청: {}", email);
        MyPageResponseDto myPage = myPageService.getMyPageInfo(email);
        return ResponseEntity.ok(myPage);
    }

    /**
     * 마이페이지 닉네임 변경
     * Patch /api/me/nickname
     */
    @Operation(summary = "마이페이지 닉네임 변경", description = "로그인한 사용자의 닉네임을 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "닉네임 변경 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @PatchMapping("/nickname")
    public ResponseEntity<Void> updateNickname(
            Principal principal,
            @Valid @RequestBody NicknameUpdateRequestDto dto
    ) {
        String email = principal.getName();
        myPageService.updateNickname(email, dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * 마이페이지 프로필 이미지 업로드 또는 교체
     * POST /api/me/profile-image
     */
    @Operation(summary = "마이페이지 프로필 이미지 업로드", description = "로그인한 사용자의 프로필 이미지를 업로드하거나 기존 이미지를 교체합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로필 이미지 업로드 또는 교체 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 요청 데이터", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 또는 파이어베이스 오류", content = @Content)
    })
    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadOrReplaceProfileImage(
            Principal principal,
            @Parameter(
                    name = "file",
                    description = "업로드할 프로필 이미지 (jpeg, png 등)",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart("file") MultipartFile file
    ) {
        String email = principal.getName();
        log.info("프로필 이미지 업로드 요청 - 이메일: {}", email);
        myPageService.updateProfileImage(email, file);
        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created 반환
    }

    /**
     * 마이페이지에서 일반 유저 권한으로 결제내역 조회 API
     * GET /api/me/payments
     */
    @Operation(summary = "마이페이지 결제내역 조회", description = "로그인한 사용자의 결제내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "결제내역 조회 성공 (결제내역이 없으면 빈 리스트 반환)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class, type = "array"),
                            examples = @ExampleObject(value = "[\n" +
                                    "  {\n" +
                                    "    \"paymentId\": 12345,\n" +
                                    "    \"amount\": 10000,\n" +
                                    "    \"paymentDate\": \"2023-10-01T12:00:00Z\",\n" +
                                    "    \"status\": \"PAID\",\n" +
                                    "    \"contentId\": 6789,\n" +
                                    "    \"contentTitle\": \"서비스 제목\",\n" +
                                    "    \"expertName\": \"전문가 닉네임\",\n" +
                                    "    \"expertProfileImageUrl\": \"https://example.com/profile.jpg\"\n" +
                                    "  }\n" +
                                    "]")
                    )
            )
    })
    @GetMapping("/payments")
    public ResponseEntity<?> getPaymentHistory(
            Principal principal,
            @RequestParam(required = false) String status
    ) {
        String email = principal.getName();
        PaymentStatus paymentStatus = null;

        if (status != null) {
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidPaymentStatusException("유효하지 않은 결제 상태입니다: " + status);
            }
        }

        log.info("결제내역 조회 요청: {}, 필터: {}", email, paymentStatus);

        List<PaymentResponseDto> payments = myPageService.getUserPayments(email, paymentStatus);
        return ResponseEntity.ok(payments);
    }
}
