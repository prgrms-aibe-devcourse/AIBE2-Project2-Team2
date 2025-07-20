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
import org.example.backend.expert.dto.request.ExpertRequestDto;
import org.example.backend.expert.dto.response.ExpertProfileDto;
import org.example.backend.expert.dto.response.ExpertSignupMetaDto;
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
    @Operation(
            summary = "전문가 회원가입 시 필요한 메타 데이터 조회",
            description = "전문가 전환 시 필요한 전문분야, 상세분야, 기술스킬 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메타 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpertSignupMetaDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"detailFields\": [\n" +
                                    "    {\n" +
                                    "      \"specialty\": \"IT/프로그래밍\",\n" +
                                    "      \"detailFields\": [\n" +
                                    "        \"UX 기획\",\n" +
                                    "        \"웹사이트 신규 제작\",\n" +
                                    "        \"웹사이트 개선/버그수정\"\n" +
                                    "      ]\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"specialty\": \"디자인\",\n" +
                                    "      \"detailFields\": [\n" +
                                    "        \"로고 디자인\",\n" +
                                    "        \"브랜드 디자인/가이드\"\n" +
                                    "      ]\n" +
                                    "    }\n" +
                                    "  ],\n" +
                                    "  \"skills\": [\n" +
                                    "    {\n" +
                                    "      \"categoryName\": \"IT/프로그래밍\",\n" +
                                    "      \"skills\": [\n" +
                                    "        \"Java\",\n" +
                                    "        \"Python\",\n" +
                                    "        \"JavaScript\"\n" +
                                    "      ]\n" +
                                    "    },\n" +
                                    "    {\n" +
                                    "      \"categoryName\": \"디자인\",\n" +
                                    "      \"skills\": [\n" +
                                    "        \"Photoshop\",\n" +
                                    "        \"Illustrator\"\n" +
                                    "      ]\n" +
                                    "    }\n" +
                                    "  ],\n" +
                                    "  \"regions\": [\n" +
                                    "    \"서울\",\n" +
                                    "    \"부산\",\n" +
                                    "    \"대구\"\n" +
                                    "  ]\n" +
                                    "}")
                    )
            )
    })
    @GetMapping("/meta")
    public ResponseEntity<ExpertSignupMetaDto> getExpertSignupMeta() {
        ExpertSignupMetaDto meta = expertService.getExpertSignupMeta();
        return ResponseEntity.ok(meta);
    }

    /**
     * 전문가 프로필 조회 API
     * 전문가의 프로필 정보를 조회합니다.
     * GET /api/expert/profile
     */
    @Operation(
            summary = "전문가 프로필 조회",
            description = "현재 로그인한 사용자의 전문가 프로필 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전문가 프로필 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ExpertProfileDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"nickname\": \"tester\",\n" +
                                    "  \"introduction\": \"자기소개\",\n" +
                                    "  \"region\": \"서울\",\n" +
                                    "  \"totalCareerYears\": 5,\n" +
                                    "  \"websiteUrl\": \"https://site.com\",\n" +
                                    "  \"facebookUrl\": \"https://facebook.com\",\n" +
                                    "  \"instagramUrl\": \"https://instagram.com\",\n" +
                                    "  \"xUrl\": \"https://x.com\",\n" +
                                    "  \"reviewCount\": 10,\n" +
                                    "  \"averageScore\": 4.5,\n" +
                                    "  \"fields\": [\n" +
                                    "    { \"specialtyName\": \"디자인\", \"detailFieldName\": \"웹/모바일 디자인\" }\n" +
                                    "  ],\n" +
                                    "  \"skills\": [\n" +
                                    "    { \"skillCategoryName\": \"IT/프로그래밍\", \"skillName\": \"Java\" }\n" +
                                    "  ],\n" +
                                    "  \"portfolios\": [\n" +
                                    "    { \"title\": \"포트폴리오 제목\", \"thumbnailUrl\": \"https://thumbnail.url/portfolio1.jpg\" }\n" +
                                    "  ],\n" +
                                    "  \"contents\": [\n" +
                                    "    { \"title\": \"컨텐츠 제목\", \"thumbnailUrl\": \"https://thumbnail.url/content1.jpg\" }\n" +
                                    "  ]\n" +
                                    "}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "전문가 프로필이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"전문가 프로필을 찾을 수 없습니다.\"")
                    )
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getExpertProfile(
            Principal principal
    ) {
        String email = principal.getName();
        log.info("전문가 프로필 조회 요청: {}", email);
        ExpertProfileDto profile = expertService.getExpertProfile(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * 전문가 프로필 업데이트 API
     * 전문가 프로필 정보를 업데이트합니다.
     * PUT /api/expert/profile
     */
    @Operation(
            summary = "전문가 프로필 수정",
            description = "기존 전문가 프로필 정보를 전체 덮어쓰기 방식으로 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "전문가 프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "회원 또는 프로필 없음")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateExpertProfile(
            @Valid @RequestBody ExpertRequestDto dto,
            Principal principal
    ) {
        String email = principal.getName();
        log.info("전문가 프로필 수정 요청: {}", email);
        expertService.updateExpertProfile(email, dto);
        return ResponseEntity.noContent().build();
    }
}
