package org.example.backend.matchinghistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.MatchingStatus;
import org.example.backend.exception.customException.InvalidMatchingStatusException;
import org.example.backend.matchinghistory.dto.request.MatchingSearchCondition;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryExpertDto;
import org.example.backend.matchinghistory.dto.response.MatchingSummaryUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching-histories")
public class MatchingHistoryController {

    private final MatchingHistoryService matchingHistoryService;

    @Operation(
            summary = "전문가 매칭 이력 조회",
            description = "로그인한 전문가의 매칭 이력 요약 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "매칭 이력 목록 조회 성공 (매칭 이력이 없으면 빈 리스트 반환)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = MatchingSummaryUserDto.class)),
                            examples = @ExampleObject(
                                    name = "userMatchingHistoriesExample",
                                    summary = "사용자 매칭 이력 예시",
                                    value = "{\n" +
                                            "  \"content\": [\n" +
                                            "    {\n" +
                                            "      \"matchingId\": 1,\n" +
                                            "      \"contentTitle\": \"로고 디자인\",\n" +
                                            "      \"contentThumbnailUrl\": \"https://example.com/thumb.png\",\n" +
                                            "      \"userName\": \"홍길동\",\n" +
                                            "      \"userPhone\": \"010-1234-5678\",\n" +
                                            "      \"matchingStatus\": \"ACCEPTED\",\n" +
                                            "      \"workStartDate\": \"2024-07-01\",\n" +
                                            "      \"workEndDate\": \"2024-07-15\",\n" +
                                            "      \"totalPrice\": 500000,\n" +
                                            "      \"selectedItems\": [\n" +
                                            "        {\"itemName\": \"서비스 A\", \"itemPrice\": 30000},\n" +
                                            "        {\"itemName\": \"서비스 B\", \"itemPrice\": 20000}\n" +
                                            "      ]\n" +
                                            "    }\n" +
                                            "  ],\n" +
                                            "  \"pageable\": {\n" +
                                            "    \"pageNumber\": 0,\n" +
                                            "    \"pageSize\": 5,\n" +
                                            "    \"offset\": 0,\n" +
                                            "    \"paged\": true,\n" +
                                            "    \"unpaged\": false\n" +
                                            "  },\n" +
                                            "  \"totalPages\": 1,\n" +
                                            "  \"totalElements\": 1,\n" +
                                            "  \"last\": true,\n" +
                                            "  \"size\": 5,\n" +
                                            "  \"number\": 0,\n" +
                                            "  \"sort\": {\n" +
                                            "    \"sorted\": true,\n" +
                                            "    \"unsorted\": false,\n" +
                                            "    \"empty\": false\n" +
                                            "  },\n" +
                                            "  \"numberOfElements\": 1,\n" +
                                            "  \"first\": true,\n" +
                                            "  \"empty\": false\n" +
                                            "}"
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (매칭상태 값 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/expert")
    public ResponseEntity<Page<MatchingSummaryUserDto>> getExpertMatchingHistories(
            Principal principal,
            @Parameter(description = "매칭 상태 필터링 (예: ACCEPTED, IN_PROGRESS, null 등)", example = "ACCEPTED")
            @RequestParam(required = false) String matchingStatusStr,
            @Parameter(description = "검색 시작 월 (yyyy-MM)", example = "2024-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth fromMonth,
            @Parameter(description = "검색 종료 월 (yyyy-MM)", example = "2025-12")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth toMonth,
            @Parameter(description = "매칭 상대방 닉네임 포함 검색", example = "tester")
            @RequestParam(required = false) String nickname,
            @Parameter(description = "특정 매칭 ID 검색", example = "1")
            @RequestParam(required = false) Long matchingId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        MatchingStatus matchingStatus = null;
        if (matchingStatusStr != null && !matchingStatusStr.isBlank()) {
            try {
                matchingStatus = MatchingStatus.valueOf(matchingStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidMatchingStatusException("유효하지 않은 매칭 상태: " + matchingStatusStr);
            }
        }

        MatchingSearchCondition condition = new MatchingSearchCondition(
                matchingStatus,
                fromMonth,
                toMonth,
                nickname,
                matchingId
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchingId"));  // 최신순 고정

        String email = principal.getName();
        Page<MatchingSummaryUserDto> result = matchingHistoryService.getExpertMatchingHistories(email, condition, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "일반 유저 매칭 이력 조회",
            description = "로그인한 일반 사용자의 매칭 이력 요약 목록을 조회한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전문가 매칭 이력 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "matchingHistoryExample",
                                            summary = "매칭 이력 예시",
                                            value =
                                                    "{\n" +
                                                            "  \"content\": [\n" +
                                                            "    {\n" +
                                                            "      \"matchingId\": 1,\n" +
                                                            "      \"contentTitle\": \"로고 디자인\",\n" +
                                                            "      \"contentThumbnailUrl\": \"https://example.com/thumb.png\",\n" +
                                                            "      \"expertName\": \"홍길동\",\n" +
                                                            "      \"expertProfileImageUrl\": \"https://example.com/profile.png\",\n" +
                                                            "      \"expertPhone\": \"010-1234-5678\",\n" +
                                                            "      \"matchingStatus\": \"ACCEPTED\",\n" +
                                                            "      \"workStartDate\": \"2024-07-01\",\n" +
                                                            "      \"workEndDate\": \"2024-07-15\",\n" +
                                                            "      \"totalPrice\": 500000,\n" +
                                                            "      \"selectedItems\": [\n" +
                                                            "        {\"itemName\": \"서비스 A\", \"itemPrice\": 30000},\n" +
                                                            "        {\"itemName\": \"서비스 B\", \"itemPrice\": 20000}\n" +
                                                            "      ]\n" +
                                                            "    }\n" +
                                                            "  ],\n" +
                                                            "  \"pageable\": {\n" +
                                                            "    \"pageNumber\": 0,\n" +
                                                            "    \"pageSize\": 5,\n" +
                                                            "    \"offset\": 0,\n" +
                                                            "    \"paged\": true,\n" +
                                                            "    \"unpaged\": false\n" +
                                                            "  },\n" +
                                                            "  \"totalPages\": 1,\n" +
                                                            "  \"totalElements\": 1,\n" +
                                                            "  \"last\": true,\n" +
                                                            "  \"size\": 5,\n" +
                                                            "  \"number\": 0,\n" +
                                                            "  \"sort\": {\n" +
                                                            "    \"sorted\": true,\n" +
                                                            "    \"unsorted\": false,\n" +
                                                            "    \"empty\": false\n" +
                                                            "  },\n" +
                                                            "  \"numberOfElements\": 1,\n" +
                                                            "  \"first\": true,\n" +
                                                            "  \"empty\": false\n" +
                                                            "}"
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (매칭상태 값 오류 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @GetMapping("/user")
    public ResponseEntity<Page<?>> getUserMatchingHistories(
            Principal principal,
            @Parameter(description = "매칭 상태 필터링 (예: ACCEPTED, IN_PROGRESS, null 등)", example = "ACCEPTED")
            @RequestParam(required = false) String matchingStatusStr,
            @Parameter(description = "검색 시작 월 (yyyy-MM)", example = "2024-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth fromMonth,
            @Parameter(description = "검색 종료 월 (yyyy-MM)", example = "2025-12")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth toMonth,
            @Parameter(description = "전문가 닉네임 포함 검색", example = "디자이너민지")
            @RequestParam(required = false) String nickname,
            @Parameter(description = "특정 매칭 ID 검색", example = "1")
            @RequestParam(required = false) Long matchingId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        MatchingStatus matchingStatus = null;
        if (matchingStatusStr != null && !matchingStatusStr.isBlank()) {
            try {
                matchingStatus = MatchingStatus.valueOf(matchingStatusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidMatchingStatusException("유효하지 않은 매칭 상태: " + matchingStatusStr);
            }
        }

        MatchingSearchCondition condition = new MatchingSearchCondition(
                matchingStatus,
                fromMonth,
                toMonth,
                nickname,
                matchingId
        );

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchingId"));

        String email = principal.getName();
        Page<MatchingSummaryExpertDto> result = matchingHistoryService.getUserMatchingHistories(email, condition, pageable);
        return ResponseEntity.ok(result);
    }

}
