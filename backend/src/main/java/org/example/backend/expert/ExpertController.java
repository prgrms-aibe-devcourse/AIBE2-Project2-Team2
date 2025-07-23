package org.example.backend.expert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.example.backend.expert.dto.response.PortfolioDetailResponseDto;
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

    /**
     * 전문가의 특정 포트폴리오 조회 API
     * 전문가의 포트폴리오 상세 정보를 조회합니다.
     * GET /api/expert/portfolio/{portfolioId}
     */
    @Operation(
            summary = "전문가 포트폴리오 상세 조회",
            description = "포트폴리오 ID를 통해 전문가의 포트폴리오 상세 정보를 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "포트폴리오 상세 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PortfolioDetailResponseDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"portfolioId\": 123,\n" +
                                    "  \"title\": \"웹사이트 개발 프로젝트\",\n" +
                                    "  \"content\": \"이 프로젝트는 ...\",\n" +
                                    "  \"viewCount\": 150,\n" +
                                    "  \"workingYear\": 3,\n" +
                                    "  \"category\": \"웹/모바일 개발\",\n" +
                                    "  \"images\": [\n" +
                                    "    {\"id\": 10, \"url\": \"https://image.url/1.jpg\"},\n" +
                                    "    {\"id\": 11, \"url\": \"https://image.url/2.jpg\"}\n" +
                                    "  ],\n" +
                                    "  \"thumbnailImage\": {\"id\": 10, \"url\": \"https://image.url/1.jpg\"},\n" +
                                    "  \"reviewCount\": 25,\n" +
                                    "  \"rating\": 4.8,\n" +
                                    "  \"expertNickname\": \"expertUser\",\n" +
                                    "  \"expertProfileImageUrl\": \"https://profile.image.url\"\n" +
                                    "}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"해당 포트폴리오가 존재하지 않습니다.\"")
                    )
            )
    })
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<PortfolioDetailResponseDto> getPortfolioDetail(
            @PathVariable Long portfolioId
    ) {
        PortfolioDetailResponseDto dto = expertService.getPortfolioDetail(portfolioId);
        return ResponseEntity.ok(dto);
    }

    /**
     * 전문가 프로필의 포트폴리오 등록 API
     * 전문가 프로필에 포트폴리오를 등록합니다.
     * POST /api/expert/portfolio
     */
    @Operation(
            summary = "전문가 포트폴리오 등록",
            description = "포트폴리오를 등록한다. 이미지 개수는 1~5개이며, 썸네일 이미지는 별도로 전송한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "포트폴리오 등록 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"포트폴리오 등록이 완료되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값 (이미지 수 초과 또는 썸네일 미전송 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    value = "\"포트폴리오 이미지는 최소 1개 이상, 최대 5개까지 업로드할 수 있습니다. 또는 썸네일 이미지를 반드시 전송해야 합니다.\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "전문가 권한이 없는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"전문가가 아닌 사용자는 포트폴리오를 생성할 수 없습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원 또는 전문가 프로필이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    value = "\"해당 이메일의 사용자가 존재하지 않습니다. 또는 전문가 프로필이 존재하지 않습니다.\""
                            )
                    )
            )
    })
    @PostMapping(value = "/portfolio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPortfolio(
            Principal principal,

            @Parameter(description = "포트폴리오 제목", required = true, example = "모던한 웹사이트 디자인")
            @RequestParam("title") String title,

            @Parameter(description = "포트폴리오 상세 내용", required = true, example = "반응형 웹사이트와 관리자 페이지 디자인 작업입니다.")
            @RequestParam("content") String content,

            @Parameter(description = "포트폴리오 카테고리", required = true, example = "디자인")
            @RequestParam("category") String category,

            @Parameter(description = "작업년도", required = true, example = "2024")
            @RequestParam("workingYear") Integer workingYear,

            @Parameter(description = "포트폴리오 이미지 파일 목록 (썸네일 제외)", required = true)
            @RequestPart("images") List<MultipartFile> images,

            @Parameter(description = "썸네일 이미지 파일", required = true)
            @RequestPart("thumbnail") MultipartFile thumbnailImage
    ) {
        String email = principal.getName();
        log.info("포트폴리오 등록 요청: {}, 제목: {}, 카테고리: {}, 경력 연수: {}, 이미지 수: {}, 썸네일 이미지 포함 여부: {}",
                email, title, category, workingYear, images.size(), (thumbnailImage != null));
        expertService.createPortfolio(email, title, content, category, workingYear, images, thumbnailImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 전문가 프로필의 포트폴리오 수정 API
     * 전문가 프로필의 기존 포트폴리오를 수정합니다.
     * PUT /api/expert/portfolio/{portfolioId}
     */
    @Operation(
            summary = "전문가 포트폴리오 수정",
            description = "기존 포트폴리오 정보를 수정한다. " +
                    "유지할 기존 이미지 ID 목록과 새 이미지 파일 목록을 함께 보내며, " +
                    "썸네일 이미지는 새 이미지 또는 유지하는 이미지 중 하나를 지정해야 한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "포트폴리오 수정 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 입력값 또는 이미지 수 초과, 혹은 썸네일 지정 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(
                                    value = "\"포트폴리오 이미지는 최소 1개 이상, 최대 5개까지 업로드할 수 있습니다.\" 또는 " +
                                            "\"썸네일로 지정한 기존 이미지가 존재하지 않습니다.\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"해당 포트폴리오가 존재하지 않습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "전문가 권한이 없는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"전문가 권한이 없습니다.\"")
                    )
            )
    })
    @PutMapping(value = "/portfolio/{portfolioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePortfolio(
            Principal principal,
            @PathVariable Long portfolioId,

            @Parameter(description = "포트폴리오 제목", required = true, example = "모던한 웹사이트 디자인")
            @RequestParam("title") String title,

            @Parameter(description = "포트폴리오 상세 내용", required = true, example = "반응형 웹사이트와 관리자 페이지 디자인 작업입니다.")
            @RequestParam("content") String content,

            @Parameter(description = "포트폴리오 카테고리", required = true, example = "디자인")
            @RequestParam("category") String category,

            @Parameter(description = "작업년도", required = true, example = "2024")
            @RequestParam("workingYear") Integer workingYear,

            @Parameter(description = "유지할 기존 이미지 ID 목록", example = "[1, 2]")
            @RequestParam("remainingImageIds") List<Long> remainingImageIds,

            @Parameter(description = "추가할 새 이미지 파일 목록")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,

            @Parameter(description = "썸네일 이미지 파일 (새 이미지로 변경 시) - 없으면 null", required = false)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailImage,

            @Parameter(description = "썸네일로 지정할 기존 이미지 ID (기존 이미지 중 변경 시)", required = false)
            @RequestParam(value = "thumbnailRemainImageId", required = false) Long thumbnailRemainImageId
    ) {
        String email = principal.getName();
        log.info("포트폴리오 수정 요청: {}, 포트폴리오 ID: {}, 제목: {}, 카테고리: {}, 경력 연수: {}, 유지 이미지 수: {}, 추가 이미지 수: {}, 썸네일 새 이미지 포함: {}, 썸네일 기존 이미지 ID: {}",
                email, portfolioId, title, category, workingYear,
                remainingImageIds.size(), images != null ? images.size() : 0,
                (thumbnailImage != null), thumbnailRemainImageId);

        expertService.updatePortfolio(
                email,
                portfolioId,
                title,
                content,
                category,
                workingYear,
                remainingImageIds,
                images,
                thumbnailImage,
                thumbnailRemainImageId
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * 전문가 포트폴리오 삭제 API
     * 전문가가 자신의 포트폴리오를 삭제합니다.
     * DELETE /api/expert/portfolio/{portfolioId}
     */
    @Operation(
            summary = "전문가 포트폴리오 삭제",
            description = "전문가가 자신의 포트폴리오를 삭제한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "포트폴리오 삭제 성공",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"포트폴리오 삭제 권한이 없습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "포트폴리오를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"해당 포트폴리오가 존재하지 않습니다.\"")
                    )
            )
    })
    @DeleteMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            Principal principal,
            @PathVariable Long portfolioId
    ) {
        String email = principal.getName();
        expertService.deletePortfolio(email, portfolioId);
        return ResponseEntity.noContent().build();
    }

}
