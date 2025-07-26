package org.example.backend.content.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.content.dto.ContentDetailResponseDto;
import org.example.backend.content.dto.ContentRequestDto;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.content.service.ContentImageService;
import org.example.backend.content.service.ContentService;
import org.example.backend.entity.Content;
import org.example.backend.entity.Member;
import org.example.backend.constant.Role;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "컨텐츠 관련 API")
public class ContentController {
    private final ContentService contentService;
    private final ContentImageService contentImageService;
    private final MemberRepository memberRepository;

    private Member getAuthenticatedMember(Principal principal) {
        String email = principal.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
    }

    @Operation(
            summary = "컨텐츠 등록",
            description = "전문가가 새로운 컨텐츠를 등록합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContentRequestDto.class),
                            examples = @ExampleObject(
                                    name = "컨텐츠 등록 예제",
                                    summary = "로고 디자인 예시",
                                    value = "{\n" +
                                            "  \"title\": \"고퀄리티 로고 디자인 제작\",\n" +
                                            "  \"description\": \"전문 디자이너가 직접 제작하는 맞춤형 로고 디자인!\",\n" +
                                            "  \"budget\": 120000,\n" +
                                            "  \"categoryId\": 1,\n" +
                                            "  \"questions\": [\n" +
                                            "    {\n" +
                                            "      \"questionText\": \"로고 스타일을 선택해주세요.\",\n" +
                                            "      \"multipleChoice\": false,\n" +
                                            "      \"options\": [\n" +
                                            "        { \"optionText\": \"심플\", \"additionalPrice\": 0 },\n" +
                                            "        { \"optionText\": \"프리미엄\", \"additionalPrice\": 20000 }\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "컨텐츠 등록 성공",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = ContentResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "전문가만 등록 가능",
                    content = @io.swagger.v3.oas.annotations.media.Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping
    public ResponseEntity<ContentResponseDto> createContent(@RequestBody ContentRequestDto requestDto, Principal principal) {
        Member member = getAuthenticatedMember(principal);
        if (member.getRole() != Role.EXPERT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ContentResponseDto response = contentService.createContent(requestDto, member);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "컨텐츠 전체 목록 조회", description = "모든 컨텐츠의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ContentResponseDto>> getAllContents() {
        List<ContentResponseDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(contents);
    }

    @Operation(summary = "컨텐츠 상세 조회", description = "특정 컨텐츠의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ContentDetailResponseDto> getContent(@PathVariable Long id, Principal principal) {
        Content content = contentService.getContentEntity(id);
        return ResponseEntity.ok(contentService.toDetailResponseDto(content));
    }


    @Operation(summary = "컨텐츠 수정", description = "특정 컨텐츠의 정보를 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContentRequestDto.class),
                            examples = @ExampleObject(
                                    name = "컨텐츠 등록 예제",
                                    summary = "로고 디자인 예시",
                                    value = "{\n" +
                                            "  \"title\": \"고퀄리티 로고 디자인 제작\",\n" +
                                            "  \"description\": \"전문 디자이너가 직접 제작하는 맞춤형 로고 디자인!\",\n" +
                                            "  \"budget\": 120000,\n" +
                                            "  \"categoryId\": 1,\n" +
                                            "  \"questions\": [\n" +
                                            "    {\n" +
                                            "      \"questionText\": \"로고 스타일을 선택해주세요.\",\n" +
                                            "      \"multipleChoice\": false,\n" +
                                            "      \"options\": [\n" +
                                            "        { \"optionText\": \"심플\", \"additionalPrice\": 0 },\n" +
                                            "        { \"optionText\": \"프리미엄\", \"additionalPrice\": 20000 }\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
    )
    @PutMapping("/{id}")
    public ResponseEntity<ContentResponseDto> updateContent(@PathVariable Long id,
                                                            @RequestBody ContentRequestDto requestDto,
                                                            Principal principal) {
        Member member = getAuthenticatedMember(principal);
        Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(contentService.updateContent(id, requestDto, member));
    }

    @Operation(summary = "컨텐츠 삭제", description = "특정 컨텐츠를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id, Principal principal) {
        Member member = getAuthenticatedMember(principal);
        Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        contentService.deleteContent(id, member);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "컨텐츠 이미지 일괄 업로드", description = "여러 장의 컨텐츠 이미지와 썸네일 이미지를 한 번에 업로드합니다. (images: 일반 이미지, thumbnail: 썸네일 이미지)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "이미지 업로드 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"이미지 업로드가 완료되었습니다.\"")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 입력값 (이미지 수 초과 또는 썸네일 미전송 등)",
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"이미지는 최소 1개 이상, 최대 5개까지 업로드할 수 있습니다. 또는 썸네일 이미지를 반드시 전송해야 합니다.\"")
            )
        )
    })
    @PostMapping(value = "/{contentId}/images/batch", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable Long contentId,
            @io.swagger.v3.oas.annotations.Parameter(description = "컨텐츠 이미지 파일 목록 (썸네일 제외)", required = true)
            @RequestPart("images") List<MultipartFile> images,
            @io.swagger.v3.oas.annotations.Parameter(description = "썸네일 이미지 파일", required = true)
            @RequestPart("thumbnail") MultipartFile thumbnailImage
    ) {
        contentImageService.uploadContentImagesBatch(contentId, images, thumbnailImage);
        return ResponseEntity.ok("이미지 업로드가 완료되었습니다.");
    }

    @Operation(summary = "컨텐츠 이미지 일괄 삭제", description = "여러 컨텐츠 이미지를 한 번에 삭제합니다. (ids: 이미지 ID 리스트)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "이미지 삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이미지 ID", content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @Schema(implementation = String.class),
            examples = @ExampleObject(value = "\"존재하지 않는 이미지 ID가 포함되어 있습니다.\"")
        ))
    })
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteImages(
            @io.swagger.v3.oas.annotations.Parameter(description = "삭제할 이미지 ID 리스트", required = true)
            @RequestParam("ids") List<Long> imageIds
    ) {
        contentImageService.deleteContentImagesBatch(imageIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "컨텐츠 이미지 전체 수정",
        description = "유지할 이미지 ID 리스트와 새 이미지, 썸네일을 함께 받아 컨텐츠 이미지 전체를 수정합니다. (remainingImageIds: 유지할 기존 이미지 ID, images: 새 이미지, thumbnail: 새 썸네일, thumbnailRemainImageId: 기존 이미지 중 썸네일 지정)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "이미지 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            schema = @Schema(implementation = String.class),
            examples = @ExampleObject(value = "\"이미지 수정 입력값 오류\"")
        ))
    })
    @PutMapping(value = "/{contentId}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateContentImages(
            @PathVariable Long contentId,
            @io.swagger.v3.oas.annotations.Parameter(description = "유지할 기존 이미지 ID 목록", example = "[1,2]")
            @RequestParam("remainingImageIds") List<Long> remainingImageIds,
            @io.swagger.v3.oas.annotations.Parameter(description = "추가할 새 이미지 파일 목록")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @io.swagger.v3.oas.annotations.Parameter(description = "썸네일 이미지 파일 (새 이미지로 변경 시)", required = false)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailImage,
            @io.swagger.v3.oas.annotations.Parameter(description = "썸네일로 지정할 기존 이미지 ID (기존 이미지 중 변경 시)", required = false)
            @RequestParam(value = "thumbnailRemainImageId", required = false) Long thumbnailRemainImageId
    ) {
        contentImageService.updateContentImages(contentId, remainingImageIds, images, thumbnailImage, thumbnailRemainImageId);
        return ResponseEntity.noContent().build();
    }

    private boolean hasContentAccess(Member member, Content content) {
        return member.getRole() == Role.ADMIN || content.getMember().getMemberId().equals(member.getMemberId());
    }
}