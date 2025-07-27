package org.example.backend.repository;

import org.example.backend.search.dto.response.SearchContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentRepositoryCustom {

    /**
     * 카테고리 ID 리스트로 컨텐츠 검색 (페이징 포함)
     * @param categoryIds 최하위 카테고리 ID 리스트
     * @param pageable 페이징 정보
     * @return 검색된 컨텐츠 응답 DTO 페이지
     */
    Page<SearchContentResponse> findContentsByCategoryIds(List<Long> categoryIds, Pageable pageable);
}