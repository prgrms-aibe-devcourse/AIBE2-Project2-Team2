package org.example.backend.category.controller;

import lombok.RequiredArgsConstructor;

import org.example.backend.category.dto.CategoryTreeDto;
import org.example.backend.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
@Tag(name = "Category", description = "카테고리 관련 API")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "카테고리 트리 조회", description = "전체 카테고리 트리를 반환합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 트리 반환",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryTreeDto.class)
            )
        )
    })
    @GetMapping("/tree")
    public List<CategoryTreeDto> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    /**
     * 특정 카테고리의 모든 최하위(leaf) 카테고리 ID들을 조회
     * 컨텐츠는 항상 최하위 카테고리에만 연결되므로, 중간 노드는 제외
     * @param categoryId 상위 카테고리 ID
     * @return 해당 카테고리 하위의 최하위 카테고리 ID 리스트
     */
    @GetMapping("/{categoryId}/leaf-category-ids")
    public ResponseEntity<List<Long>> getLeafCategoryIds(@PathVariable Long categoryId) {
        List<Long> leafCategoryIds = categoryService.getAllSubCategoryIds(categoryId);
        return ResponseEntity.ok(leafCategoryIds);
    }

    /**
     * 특정 카테고리로 컨텐츠 조회 (추후 ContentController로 이동 예정)
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리와 하위 카테고리의 모든 컨텐츠
     */
    @GetMapping("/{categoryId}/contents")
    public ResponseEntity<String> getContentsByCategory(@PathVariable Long categoryId) {
        List<Long> categoryIds = categoryService.getAllSubCategoryIds(categoryId);

        // TODO: ContentService.getContentsByCategoryIds(categoryIds) 호출
        return ResponseEntity.ok("카테고리 ID들: " + categoryIds.toString() + " 로 컨텐츠 조회 예정");
    }
}