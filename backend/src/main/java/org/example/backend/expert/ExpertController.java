package org.example.backend.expert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.expert.dto.ExpertRequestDto;
import org.example.backend.expert.dto.ExpertSignupMetaDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/expert")
@Tag(name = "Expert", description = "전문가 관련 API")
public class ExpertController {
    private final ExpertService expertService;

    /**
     * 일반유저에서 전문가로 전환하는 API
     * 전문가로 전환하기 위해서는 일반 사용자 계정이 필요합니다.
     */
    @Operation(summary = "전문가로 전환", description = "일반 사용자 계정을 전문가 계정으로 전환합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "전문가로 성공적으로 전환됨",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 - 입력값 오류 등",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"잘못된 요청입니다.\"")
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 전문가로 등록된 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"이미 전문가로 등록된 사용자입니다.\"")
                    )
            )
    })
    @PostMapping("/upgrade")
    public ResponseEntity<?> upgradeExpert(
            @Valid @RequestBody ExpertRequestDto expertRequestDto,
            Principal principal
    ){
        String email = principal.getName();
        log.info("전문가 전환 요청: {}", expertRequestDto);
        expertService.upgradeToExpert(email, expertRequestDto);
        return ResponseEntity.noContent().build();
    }

    /**
     * 전문가 전환을 위해서 고를 수 있는 전문분야, 상세분야, 기술스킬 목록을 반환하는 API
     */
    @GetMapping("/meta")
    public ResponseEntity<?> getExpertSignupMeta() {
        ExpertSignupMetaDto meta = expertService.getExpertSignupMeta();
        return ResponseEntity.ok(meta);
    }
}
