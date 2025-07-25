package org.example.backend.category.controller;

import lombok.RequiredArgsConstructor;

import org.example.backend.category.dto.CategoryTreeDto;
import org.example.backend.category.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
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
}