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
import io.swagger.v3.oas.annotations.Parameter;
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
            description = "전문가가 새로운 컨텐츠를 등록합니다."
    )
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

    @Operation(summary = "컨텐츠 수정", description = "특정 컨텐츠의 정보를 수정합니다.")
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

    @Operation(summary = "컨텐츠 이미지 일괄 업로드", description = "여러 장의 컨텐츠 이미지와 썸네일 이미지를 한 번에 업로드합니다.")
    @PostMapping(value = "/{contentId}/images/batch", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImages(
            @PathVariable Long contentId,
            @Parameter(description = "컨텐츠 이미지 파일 목록 (썸네일 제외)", required = true)
            @RequestPart("images") List<MultipartFile> images,
            @Parameter(description = "썸네일 이미지 파일", required = true)
            @RequestPart("thumbnail") MultipartFile thumbnailImage
    ) {
        contentImageService.uploadContentImagesBatch(contentId, images, thumbnailImage);
        return ResponseEntity.ok("이미지 업로드가 완료되었습니다.");
    }

    @Operation(summary = "컨텐츠 이미지 일괄 삭제", description = "여러 컨텐츠 이미지를 한 번에 삭제합니다.")
    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteImages(
            @Parameter(description = "삭제할 이미지 ID 리스트", required = true)
            @RequestParam("ids") List<Long> imageIds
    ) {
        contentImageService.deleteContentImagesBatch(imageIds);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "컨텐츠 이미지 전체 수정", description = "유지할 이미지 ID 리스트와 새 이미지, 썸네일을 함께 받아 컨텐츠 이미지 전체를 수정합니다.")
    @PutMapping(value = "/{contentId}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateContentImages(
            @PathVariable Long contentId,
            @Parameter(description = "유지할 기존 이미지 ID 목록")
            @RequestParam("remainingImageIds") List<Long> remainingImageIds,
            @Parameter(description = "추가할 새 이미지 파일 목록")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(description = "썸네일 이미지 파일 (새 이미지로 변경 시)", required = false)
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailImage,
            @Parameter(description = "썸네일로 지정할 기존 이미지 ID (기존 이미지 중 변경 시)", required = false)
            @RequestParam(value = "thumbnailRemainImageId", required = false) Long thumbnailRemainImageId
    ) {
        contentImageService.updateContentImages(contentId, remainingImageIds, images, thumbnailImage, thumbnailRemainImageId);
        return ResponseEntity.noContent().build();
    }

    private boolean hasContentAccess(Member member, Content content) {
        return member.getRole() == Role.ADMIN || content.getMember().getMemberId().equals(member.getMemberId());
    }
}