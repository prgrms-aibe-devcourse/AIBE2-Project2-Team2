package org.example.backend.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.category.service.CategoryService;
import org.example.backend.repository.ContentRepository;
import org.example.backend.search.dto.response.SearchContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final CategoryService categoryService;
    private final ContentRepository contentRepository;

    /**
     * 카테고리 ID로 컨텐츠 검색 (페이징 포함)
     * @param categoryId 검색할 카테고리 ID
     * @param pageable 페이징 정보
     * @return 검색된 컨텐츠 목록
     */
    public Page<SearchContentResponse> searchByCategory(Long categoryId, Pageable pageable) {
        log.info("카테고리 검색 시작 - categoryId: {}", categoryId);

        // 1단계: 해당 카테고리의 모든 최하위 카테고리 ID들 조회
        List<Long> leafCategoryIds = categoryService.getAllSubCategoryIds(categoryId);
        log.info("최하위 카테고리 ID 수집 완료 - count: {}, ids: {}", leafCategoryIds.size(), leafCategoryIds);

        // 2단계: 수집된 카테고리 ID들로 컨텐츠 조회 (페이징 포함)
        Page<SearchContentResponse> result = contentRepository.findContentsByCategoryIds(leafCategoryIds, pageable);

        log.info("컨텐츠 검색 완료 - 전체: {}, 현재 페이지: {}", result.getTotalElements(), result.getContent().size());
        return result;
    }
}