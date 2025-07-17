package org.example.backend.content.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.content.dto.ContentRequestDto;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.content.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.example.backend.entity.Member;
import org.example.backend.constant.Role;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import java.security.Principal;
import org.example.backend.entity.Content;
import org.example.backend.exception.customException.NoContentPermissionException;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class ContentController {
    private final ContentService contentService;
    private final MemberRepository memberRepository;

    // 컨텐츠 등록
    @PostMapping
    public ResponseEntity<ContentResponseDto> createContent(@RequestBody ContentRequestDto requestDto, Principal principal) {
        // 인증 정보에서 이메일 추출
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // Role 체크
        if (member.getRole() != Role.EXPERT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ContentResponseDto response = contentService.createContent(requestDto, member);
        return ResponseEntity.ok(response);
    }

    // 컨텐츠 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<ContentResponseDto>> getAllContents() {
        List<ContentResponseDto> contents = contentService.getAllContents();
        return ResponseEntity.ok(contents);
    }

    // 컨텐츠 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ContentResponseDto> getContent(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            throw new NoContentPermissionException("해당 컨텐츠에 대한 권한이 없습니다.");
        }
        return ResponseEntity.ok(contentService.toResponseDto(content));
    }

    // 컨텐츠 수정
    @PutMapping("/{id}")
    public ResponseEntity<ContentResponseDto> updateContent(@PathVariable Long id,
                                                            @RequestBody ContentRequestDto requestDto,
                                                            Principal principal) {
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            throw new NoContentPermissionException("해당 컨텐츠에 대한 권한이 없습니다.");
        }
        ContentResponseDto response = contentService.updateContent(id, requestDto, member);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id,
                                              Principal principal) {
        String email = principal.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));
        Content content = contentService.getContentEntity(id);
        if (!hasContentAccess(member, content)) {
            throw new NoContentPermissionException("해당 컨텐츠에 대한 권한이 없습니다.");
        }
        contentService.deleteContent(id, member);
        return ResponseEntity.noContent().build();
    }

    // 권한 체크 메서드
    private boolean hasContentAccess(Member member, Content content) {
        return member.getRole() == Role.ADMIN || content.getMember().getMemberId().equals(member.getMemberId());
    }
}
