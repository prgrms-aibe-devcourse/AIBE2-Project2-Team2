package org.example.backend.content.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.content.dto.ContentRequestDto;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.content.service.ContentImageService;
import org.example.backend.content.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import org.example.backend.entity.Member;
import org.example.backend.constant.Role;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Tag(name = "Content", description = "컨텐츠 관련 API")
public class ContentController {
    private final ContentService contentService;
    private final ContentImageService contentImageService;
    private final MemberRepository memberRepository;

    private Member getAuthenticatedMember(Principal principal){
        String email = principal.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
    }
    // 컨텐츠 등록
    @Operation(summary = "컨텐츠 등록", description = "전문가가 새로운 컨텐츠를 등록합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 등록 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentResponseDto.class),
                examples = @ExampleObject(value = "{\"id\":1,\"title\":\"예시 제목\",...}")
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "전문가만 등록 가능",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"권한이 없습니다.\"")
            )
        )
    })
    @PostMapping
    public ResponseEntity<ContentResponseDto> createContent(@RequestBody ContentRequestDto requestDto, Principal principal) {

        System.out.println("categoryId: " + requestDto.getCategoryId());
        // 인증 정보에서 이메일 추출
        Member member = getAuthenticatedMember(principal);
        // Role 체크
        if (member.getRole() != Role.EXPERT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ContentResponseDto response = contentService.createContent(requestDto, member);
        return ResponseEntity.ok(response);
    }

    // 컨텐츠 전체 목록 조회
    @Operation(summary = "컨텐츠 전체 목록 조회", description = "모든 컨텐츠의 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 목록 반환",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentResponseDto.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<List<ContentResponseDto>> getAllContents() {
        List<ContentResponseDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(contents);
    }

    // 컨텐츠 상세 조회
    @Operation(summary = "컨텐츠 상세 조회", description = "특정 컨텐츠의 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 상세 반환",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"권한이 없습니다.\"")
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContentResponseDto> getContent(@PathVariable Long id, Principal principal) {
        Member member = getAuthenticatedMember(principal);
        org.example.backend.entity.Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(contentService.toResponseDto(content));
    }

    // 컨텐츠 수정
    @Operation(summary = "컨텐츠 수정", description = "특정 컨텐츠의 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "컨텐츠 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContentResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"권한이 없습니다.\"")
            )
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ContentResponseDto> updateContent(@PathVariable Long id,
                                                            @RequestBody ContentRequestDto requestDto,
                                                            Principal principal) {
        Member member = getAuthenticatedMember(principal);
        org.example.backend.entity.Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ContentResponseDto response = contentService.updateContent(id, requestDto, member);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "컨텐츠 삭제", description = "특정 컨텐츠를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "컨텐츠 삭제 성공",
            content = @Content()
        ),
        @ApiResponse(
            responseCode = "403",
            description = "접근 권한 없음",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"권한이 없습니다.\"")
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id,
                                              Principal principal) {
        Member member = getAuthenticatedMember(principal);
        org.example.backend.entity.Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        contentService.deleteContent(id, member);
        return ResponseEntity.noContent().build();
    }

    // 이미지 업로드
    @Operation(
            summary = "컨텐츠 이미지 업로드",
            description = "특정 컨텐츠에 이미지를 업로드합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(type = "object")
                    )
            )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "이미지 업로드 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class),
                examples = @ExampleObject(value = "\"이미지 URL\"")
            )
        )
    })
    @PostMapping("/{contentId}/images")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long contentId,
            @Parameter(description = "업로드할 이미지 파일", required = true, schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "이미지 순서", required = true)
            @RequestParam("order") byte order) {
        String imageUrl = contentImageService.uploadContentImage(contentId, file, order);
        return ResponseEntity.ok(imageUrl);
    }

    // 이미지 삭제
    @Operation(summary = "컨텐츠 이미지 삭제", description = "특정 컨텐츠 이미지를 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "이미지 삭제 성공",
            content = @Content()
        )
    })
    @DeleteMapping("/images/{contentImageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long contentImageId) {
        contentImageService.deleteContentImage(contentImageId);
        return ResponseEntity.noContent().build();
    }


    // 권한 체크 메서드
    private boolean hasContentAccess(Member member, org.example.backend.entity.Content content) {
        return member.getRole() == Role.ADMIN || content.getMember().getMemberId().equals(member.getMemberId());
    }
}
