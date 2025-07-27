package org.example.backend.content.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.example.backend.content.dto.ContentResponseDto;
import org.example.backend.content.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/content")
public class ContentQueryController {

    private final ContentService contentService;

    @Operation(
            summary = "카테고리별 콘텐츠 목록 조회 (페이징)",
            description = "특정 카테고리에 속한 ACTIVE 상태의 콘텐츠를 페이지네이션하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "콘텐츠 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = ContentResponseDto.class)),
                            examples = @ExampleObject(
                                    name = "contentListPageExample",
                                    summary = "카테고리 콘텐츠 페이지 예시",
                                    value = "{\n" +
                                            "  \"content\": [\n" +
                                            "    {\n" +
                                            "      \"contentId\": 1,\n" +
                                            "      \"memberId\": 3,\n" +
                                            "      \"title\": \"로고 디자인\",\n" +
                                            "      \"description\": \"브랜드 맞춤형 로고 디자인 서비스\",\n" +
                                            "      \"budget\": 150000,\n" +
                                            "      \"status\": \"ACTIVE\",\n" +
                                            "      \"regTime\": \"2024-07-25T10:30:00\",\n" +
                                            "      \"updateTime\": \"2024-07-25T10:30:00\",\n" +
                                            "      \"createdBy\": \"expert@example.com\",\n" +
                                            "      \"modifiedBy\": \"admin@example.com\",\n" +
                                            "      \"categoryId\": 5,\n" +
                                            "      \"categoryName\": \"로고 디자인\",\n" +
                                            "      \"contentUrl\": \"https://example.com/thumb.jpg\",\n" +
                                            "      \"imageUrls\": [\n" +
                                            "        \"https://example.com/thumb.jpg\",\n" +
                                            "        \"https://example.com/img2.jpg\"\n" +
                                            "      ],\n" +
                                            "      \"expertName\": \"김전문\",\n" +
                                            "      \"rating\": 4.7,\n" +
                                            "      \"reviewCount\": 23\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"pageable\": {\n" +
                                            "    \"pageNumber\": 0,\n" +
                                            "    \"pageSize\": 12\n" +
                                            "  },\n" +
                                            "  \"totalPages\": 3,\n" +
                                            "  \"totalElements\": 25,\n" +
                                            "  \"last\": false,\n" +
                                            "  \"first\": true,\n" +
                                            "  \"size\": 12,\n" +
                                            "  \"numberOfElements\": 12\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
    })
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ContentResponseDto>> getContentsByCategory(
            @Parameter(description = "카테고리 ID", example = "1")
            @PathVariable Long categoryId,

            @Parameter(hidden = true)
            @PageableDefault(size = 12) Pageable pageable
    ) {
        Page<ContentResponseDto> result = contentService.getContentsByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(result);
    }
}
