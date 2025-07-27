package org.example.backend.content.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.constant.Role;
import org.example.backend.content.dto.ContentDetailResponseDto;
import org.example.backend.content.dto.ContentRequestDto;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.constant.Status;
import org.example.backend.entity.*;
import org.example.backend.repository.CategoryRepository;
import org.example.backend.repository.ContentRepository;
import org.example.backend.exception.customException.ContentNotFoundException;
import org.example.backend.exception.customException.NoContentPermissionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {
    private final ContentRepository contentRepository;
    private final CategoryRepository categoryRepository;

    // 작성자 확인
    private void verifyContentPermission(Content content, Member member){
        boolean isOwner = content.getMember().getMemberId().equals(member.getMemberId());
        boolean isAdmin = member.getRole() == Role.ADMIN;

        if(!isOwner && !isAdmin){
            throw new NoContentPermissionException("권한이 없습니다.");
        }
    }

    // 컨텐츠 등록
    public ContentResponseDto createContent(ContentRequestDto requestDto, Member member) {
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        Content content = Content.builder()
                .member(member)
                .title(requestDto.getTitle())
                .description(requestDto.getDescription())
                .budget(requestDto.getBudget())
                .category(category)
                .status(Status.ACTIVE) // 생성 시 ACTIVE로 설정
                .build();

        List<Question> questions = requestDto.getQuestions().stream()
                .map(qDto -> {
                    Question question = new Question(content, qDto.getQuestionText(), qDto.isMultipleChoice());
                    List<QuestionOption> options = qDto.getOptions().stream()
                            .map(oDto -> new QuestionOption(oDto.getOptionText(), oDto.getAdditionalPrice()))
                            .collect(Collectors.toList());
                    options.forEach(question::addOption);
                    return question;
                }).collect(Collectors.toList());

        content.setQuestions(questions);

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

        // 작성자(권한) 확인
        verifyContentPermission(content, member);

        //카테고리 엔티티 조회
        Category category = categoryRepository.findById(requestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리가 존재하지 않습니다."));

        //컨텐츠 값들 업데이트
        content.updateContent(
                requestDto.getTitle(),
                requestDto.getDescription(),
                requestDto.getBudget(),
                category
        );

        // 기존 questions 삭제 (개별적으로 삭제)
        List<Question> existingQuestions = new ArrayList<>(content.getQuestions());
        for (Question question : existingQuestions) {
            content.getQuestions().remove(question);
        }

        // 새로운 questions 추가
        List<Question> questions = requestDto.getQuestions().stream()
                .map(qDto -> {
                    Question question = new Question(content, qDto.getQuestionText(), qDto.isMultipleChoice());
                    List<QuestionOption> options = qDto.getOptions().stream()
                            .map(oDto -> new QuestionOption(oDto.getOptionText(), oDto.getAdditionalPrice()))
                            .collect(Collectors.toList());
                    options.forEach(question::addOption);
                    return question;
                }).collect(Collectors.toList());

        // 새로운 questions를 content에 추가
        for (Question question : questions) {
            content.getQuestions().add(question);
        }

        Content updatedContent = contentRepository.save(content);
        return toResponseDto(updatedContent);
    }

    // 컨텐츠 삭제 (status를 DELETED로 변경) soft delete 방식
    public void deleteContent(Long id, Member member) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("컨텐츠를 찾을 수 없습니다."));

        // 작성자 확인
        verifyContentPermission(content, member);

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

    // 카테고리 전체 경로를 가져오는 메서드
    private String getCategoryFullPath(Category category) {
        if (category == null) return "";
        
        StringBuilder path = new StringBuilder(category.getName());
        Category current = category.getParent();
        
        while (current != null) {
            path.insert(0, current.getName() + " > ");
            current = current.getParent();
        }
        
        return path.toString();
    }

    // 엔티티 → DTO 변환 (컨트롤러에서 사용 가능하도록 public)
    public ContentResponseDto toResponseDto(Content content) {
        List<String> imageUrls = content.getImages() != null
                ? content.getImages().stream()
                    .sorted((img1, img2) -> Byte.compare(img1.getOrderIndex(), img2.getOrderIndex()))
                    .map(ContentImage::getImageUrl)
                    .collect(Collectors.toList())
                : List.of();

        // 대표 이미지(썸네일) URL 추출
        String contentUrl = null;
        if (content.getImages() != null && !content.getImages().isEmpty()) {
            contentUrl = content.getImages().stream()
                    .filter(ContentImage::isThumbnail)
                    .map(ContentImage::getImageUrl)
                    .findFirst()
                    .orElse(content.getImages().get(0).getImageUrl());
        }

        Category category = content.getCategory();  // null 방지
        Long categoryId = category != null ? category.getCategoryId() : null;
        String categoryName = category != null ? getCategoryFullPath(category) : null;

        return ContentResponseDto.builder()
                .contentId(content.getContentId())
                .memberId(content.getMember().getMemberId())
                .title(content.getTitle())
                .description(content.getDescription())
                .budget(content.getBudget())
                .status(content.getStatus() != null ? content.getStatus().name() : null)
                .regTime(content.getRegTime() != null ? content.getRegTime().toString() : null)
                .updateTime(content.getUpdateTime() != null ? content.getUpdateTime().toString() : null)
                .createdBy(content.getCreatedBy())
                .modifiedBy(content.getModifiedBy())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .imageUrls(imageUrls)
                .contentUrl(contentUrl)
                .build();
    }
    public ContentDetailResponseDto toDetailResponseDto(Content content) {
        List<ContentDetailResponseDto.QuestionDto> questionDtos = content.getQuestions().stream()
                .map(question -> {
                    List<ContentDetailResponseDto.QuestionDto.OptionDto> optionDtos = question.getOptions().stream()
                            .map(option -> ContentDetailResponseDto.QuestionDto.OptionDto.builder()
                                    .optionId(option.getId())
                                    .optionText(option.getOptionText())
                                    .additionalPrice(option.getAdditionalPrice())
                                    .build())
                            .collect(Collectors.toList());

                    return ContentDetailResponseDto.QuestionDto.builder()
                            .questionId(question.getId())
                            .questionText(question.getQuestionText())
                            .isMultipleChoice(question.isMultipleChoice())
                            .options(optionDtos)
                            .build();
                })
                .collect(Collectors.toList());

        // 모든 이미지 URL 추출 (orderIndex 순서로 정렬하여 썸네일이 먼저 나오도록)
        List<String> imageUrls = content.getImages() != null
                ? content.getImages().stream()
                    .sorted((img1, img2) -> Byte.compare(img1.getOrderIndex(), img2.getOrderIndex()))
                    .map(ContentImage::getImageUrl)
                    .collect(Collectors.toList())
                : List.of();

        // 대표 이미지(썸네일) URL 추출
        String contentUrl = null;
        if (content.getImages() != null && !content.getImages().isEmpty()) {
            contentUrl = content.getImages().stream()
                    .filter(ContentImage::isThumbnail)
                    .map(ContentImage::getImageUrl)
                    .findFirst()
                    .orElse(content.getImages().get(0).getImageUrl());
        }

        Category category = content.getCategory();
        Long categoryId = category != null ? category.getCategoryId() : null;
        String categoryName = category != null ? getCategoryFullPath(category) : null;
        Long expertId = content.getMember() != null ? content.getMember().getMemberId() : null;
        String expertEmail = content.getMember() != null ? content.getMember().getEmail() : null;
        String expertNickname = content.getMember() != null ? content.getMember().getNickname() : null;
        String expertProfileImageUrl = content.getMember() != null ? content.getMember().getProfileImageUrl() : null;

        // 포트폴리오 썸네일/제목 리스트 생성
        List<ContentDetailResponseDto.SimplePortfolioDto> portfolioDtos = null;
        if (content.getMember() != null && content.getMember().getExpertProfile() != null) {
            portfolioDtos = content.getMember().getExpertProfile().getPortfolios().stream()
                    .map(p -> {
                        String thumbnailUrl = null;
                        if (p.getImages() != null && !p.getImages().isEmpty()) {
                            thumbnailUrl = p.getImages().stream()
                                    .filter(img -> img.isThumbnailCheck())
                                    .map(PortfolioImage::getImageUrl)
                                    .findFirst()
                                    .orElse(p.getImages().get(0).getImageUrl());
                        }
                        return ContentDetailResponseDto.SimplePortfolioDto.builder()
                                .portfolioId(p.getPortfolioId())
                                .title(p.getTitle())
                                .thumbnailUrl(thumbnailUrl)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        return ContentDetailResponseDto.builder()
                .contentId(content.getContentId())
                .title(content.getTitle())
                .description(content.getDescription())
                .budget(content.getBudget())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .expertId(expertId)
                .expertEmail(expertEmail)
                .expertNickname(expertNickname)
                .expertProfileImageUrl(expertProfileImageUrl)
                .questions(questionDtos)
                .contentUrl(contentUrl)
                .imageUrls(imageUrls)
                .portfolios(portfolioDtos)
                .build();
    }

    // 전문가의 다른 콘텐츠 목록 조회
    @Transactional(readOnly = true)
    public List<ContentResponseDto> getContentsByExpert(Long expertId) {
        return contentRepository.findByMember_MemberIdAndStatus(expertId, Status.ACTIVE)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}
