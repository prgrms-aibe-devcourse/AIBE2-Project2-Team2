package org.example.backend.search;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.search.dto.response.SearchContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<Page<?>> getCategories(
            @PathVariable Long categoryId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("카테고리 검색 시작 - 카테고리 ID: {}, 페이지: {}, 사이즈: {}", categoryId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<SearchContentResponse> result = searchService.searchByCategory(categoryId, pageable);

        log.info("카테고리 검색 완료 - 결과 수: {}", result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    /**
     * 키워드로 컨텐츠 검색
     * @param keyword 검색 키워드
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 검색된 컨텐츠 목록
     */
    @GetMapping("/keyword")
    public ResponseEntity<Page<SearchContentResponse>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        log.info("키워드 검색 요청 - 키워드: '{}', 페이지: {}, 사이즈: {}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updateTime"));
        Page<SearchContentResponse> result = searchService.searchByKeyword(keyword, pageable);

        log.info("키워드 검색 응답 - 전체: {}, 현재 페이지: {}", result.getTotalElements(), result.getContent().size());
        return ResponseEntity.ok(result);
    }
}
