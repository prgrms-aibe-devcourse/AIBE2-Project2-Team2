package org.example.backend.content.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.content.dto.ContentRequestDto;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.constant.Status;
import org.example.backend.entity.Content;
import org.example.backend.entity.Member;
import org.example.backend.repository.ContentRepository;
import org.example.backend.exception.customException.ContentNotFoundException;
import org.example.backend.exception.customException.NoContentPermissionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {
    private final ContentRepository contentRepository;

    // 컨텐츠 등록
    public ContentResponseDto createContent(ContentRequestDto requestDto, Member member) {
        Content content = Content.builder()
                .member(member)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .budget(requestDto.getBudget())
                .category(requestDto.getCategory())
                .status(Status.ACTIVE) // 생성 시 ACTIVE로 설정
                .build();
        
        Content savedContent = contentRepository.save(content);
        return toResponseDto(savedContent);
    }

    // 컨텐츠 전체 목록 조회 (ACTIVE 상태만)
    @Transactional(readOnly = true)
    public List<ContentResponseDto> getAllContents() {
        return contentRepository.findByStatus(Status.ACTIVE).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    // 컨텐츠 상세 조회
    @Transactional(readOnly = true)
    public ContentResponseDto getContent(Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다."));
        return toResponseDto(content);
    }

    // 컨텐츠 수정
    public ContentResponseDto updateContent(Long id, ContentRequestDto requestDto, Member member) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!content.getMember().getMemberId().equals(member.getMemberId())) {
            throw new NoContentPermissionException("컨텐츠 수정 권한이 없습니다.");
        }
        
        content.updateContent(
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getBudget(),
                requestDto.getCategory()
        );
        
        Content updatedContent = contentRepository.save(content);
        return toResponseDto(updatedContent);
    }

    // 컨텐츠 삭제 (status를 DELETED로 변경) soft delete 방식
    public void deleteContent(Long id, Member member) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다."));
        
        // 작성자 확인
        if (!content.getMember().getMemberId().equals(member.getMemberId())) {
            throw new NoContentPermissionException("컨텐츠 삭제 권한이 없습니다.");
        }
        
        // status를 DELETED로 변경
        content.setStatus(Status.DELETED);
        contentRepository.save(content);
    }

    // 엔티티 반환 (권한 체크용)
    @Transactional(readOnly = true)
    public Content getContentEntity(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다."));
    }

    // 엔티티 → DTO 변환 (컨트롤러에서 사용 가능하도록 public)
    public ContentResponseDto toResponseDto(Content content) {
        return ContentResponseDto.builder()
                .contentId(content.getContentId())
                .memberId(content.getMember().getMemberId())
                .title(content.getTitle())
                .description(content.getDescription())
                .budget(content.getBudget())
                .status(content.getStatus().name())
                .regTime(content.getRegTime().toString())
                .updateTime(content.getUpdateTime().toString())
                .createdBy(content.getCreatedBy())
                .modifiedBy(content.getModifiedBy())
                .category(content.getCategory())
                .build();
    }
}
