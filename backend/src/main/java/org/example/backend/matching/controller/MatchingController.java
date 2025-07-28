package org.example.backend.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.matching.dto.MatchingRequestDto;
import org.example.backend.matching.dto.MatchingResponseDto;
import org.example.backend.matching.dto.MatchingStatusUpdateDto;
import org.example.backend.matching.service.MatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matchings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Matching", description = "클라이언트와 전문가 간 매칭 기능 API")
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 매칭 요청 생성 API (→ WAITING_PAYMENT)
     *
     * 클라이언트가 전문가의 콘텐츠를 선택하고 견적 항목을 지정하여 매칭을 요청합니다.
     * 매칭 요청이 생성되면 상태는 WAITING_PAYMENT가 되며, 결제 단계로 진입합니다.
     *
     * @param requestDto 매칭 요청에 필요한 회원 ID, 콘텐츠 ID, 견적 항목 및 금액이 포함된 DTO
     * @return 생성된 매칭 정보 (WAITING_PAYMENT 상태)
     */
    @Operation(summary = "매칭 생성", description = "클라이언트가 전문가의 콘텐츠에 대해 매칭을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "매칭 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"matchingId\": 1,\n" +
                                    "  \"memberId\": 2,\n" +
                                    "  \"contentId\": 3,\n" +
                                    "  \"status\": \"WAITING_PAYMENT\",\n" +
                                    "  \"totalPrice\": 150000\n" +
                                    "}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "회원 또는 콘텐츠 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "\"회원을 찾을 수 없습니다.\"")))
    })
    @PostMapping
    public ResponseEntity<MatchingResponseDto> createMatching(@Valid @RequestBody MatchingRequestDto requestDto) {
        // 로그인 유저 email 추출
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[POST] 매칭 생성 요청: email={}, contentId={}", email, requestDto.getContentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(matchingService.createMatching(requestDto, email));
    }

    /**
     * 매칭 상태 변경 API (예: WAITING_PAYMENT → CANCELLED, ACCEPTED → REJECTED)
     *
     * 전문가 또는 시스템이 매칭 상태를 변경합니다. 거절 또는 취소 시 사유를 함께 전달할 수 있습니다.
     * 상태에 따라 적절한 예외 응답이 반환됩니다.
     *
     * @param matchingId 매칭 ID
     * @param statusDto 변경할 상태 및 사유 정보가 담긴 DTO
     * @return 변경된 매칭 정보
     */
    @Operation(summary = "매칭 상태 변경", description = "전문가 또는 시스템이 매칭 상태를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"REJECTED\" }"))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태 변경 요청",
                    content = @Content(examples = @ExampleObject(value = "\"허용되지 않은 상태 변경입니다.\""))),
            @ApiResponse(responseCode = "404", description = "매칭 없음",
                    content = @Content(examples = @ExampleObject(value = "\"매칭을 찾을 수 없습니다.\"")))
    })
    @PatchMapping("/{matchingId}/status")
    public ResponseEntity<MatchingResponseDto> updateMatchingStatus(
            @PathVariable Long matchingId,
            @Valid @RequestBody MatchingStatusUpdateDto statusDto) {
        log.info("[PATCH] 매칭 상태 변경 요청: id={}, status={}, reason={}", matchingId, statusDto.getStatus(), statusDto.getReason());
        return ResponseEntity.ok(matchingService.updateMatchingStatus(matchingId, statusDto));
    }

    /**
     * 전문가가 작업을 시작합니다. (ACCEPTED → IN_PROGRESS)
     *
     * 결제 완료된 매칭에 대해 전문가가 실제 작업을 시작할 때 사용합니다.
     * 상태가 ACCEPTED일 때만 IN_PROGRESS로 전이 가능합니다.
     *
     * @param matchingId 작업을 시작할 매칭 ID
     * @return IN_PROGRESS 상태로 변경된 매칭 정보
     */
    @Operation(summary = "작업 시작", description = "전문가가 매칭 작업을 시작합니다. (ACCEPTED → IN_PROGRESS)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 시작 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"IN_PROGRESS\" }"))),
            @ApiResponse(responseCode = "400", description = "작업 시작 불가",
                    content = @Content(examples = @ExampleObject(value = "\"ACCEPTED 상태에서만 작업을 시작할 수 있습니다.\""))),
            @ApiResponse(responseCode = "404", description = "매칭 없음",
                    content = @Content(examples = @ExampleObject(value = "\"매칭을 찾을 수 없습니다.\"")))
    })
    @PatchMapping("/{matchingId}/start")
    public ResponseEntity<MatchingResponseDto> startWork(@PathVariable Long matchingId) {
        log.info("[PATCH] 작업 시작 요청: id={}", matchingId);
        return ResponseEntity.ok(matchingService.startWork(matchingId));
    }

    /**
     * 전문가가 작업을 완료합니다. (IN_PROGRESS → WORK_COMPLETED)
     *
     * 전문가가 작업을 완료하고 결과물을 제출했을 때 호출되는 API입니다.
     * 상태가 IN_PROGRESS일 때만 WORK_COMPLETED로 변경됩니다.
     *
     * @param matchingId 작업을 완료할 매칭 ID
     * @return WORK_COMPLETED 상태로 변경된 매칭 정보
     */
    @Operation(summary = "작업 완료 처리", description = "전문가가 작업을 완료합니다. (IN_PROGRESS → WORK_COMPLETED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 완료 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"WORK_COMPLETED\" }"))),
            @ApiResponse(responseCode = "400", description = "작업 완료 불가",
                    content = @Content(examples = @ExampleObject(value = "\"IN_PROGRESS 상태에서만 완료할 수 있습니다.\""))),
            @ApiResponse(responseCode = "404", description = "매칭 없음",
                    content = @Content(examples = @ExampleObject(value = "\"매칭을 찾을 수 없습니다.\"")))
    })
    @PatchMapping("/{matchingId}/complete")
    public ResponseEntity<MatchingResponseDto> completeWork(@PathVariable Long matchingId) {
        log.info("[PATCH] 작업 완료 요청: id={}", matchingId);
        return ResponseEntity.ok(matchingService.completeWork(matchingId));
    }

    /**
     * 클라이언트가 작업 완료를 승인합니다. (WORK_COMPLETED → CONFIRMED)
     *
     * 클라이언트가 전문가의 작업 결과를 검토하고 승인하는 단계입니다.
     * 상태가 WORK_COMPLETED일 때만 CONFIRMED 상태로 전이됩니다.
     *
     * @param matchingId 최종 승인을 수행할 매칭 ID
     * @return CONFIRMED 상태로 변경된 매칭 정보
     */
    @Operation(summary = "작업 완료 승인", description = "클라이언트가 작업 완료를 승인합니다. (WORK_COMPLETED → CONFIRMED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작업 승인 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"CONFIRMED\" }"))),
            @ApiResponse(responseCode = "400", description = "작업 승인 불가",
                    content = @Content(examples = @ExampleObject(value = "\"WORK_COMPLETED 상태에서만 승인할 수 있습니다.\""))),
            @ApiResponse(responseCode = "404", description = "매칭 없음",
                    content = @Content(examples = @ExampleObject(value = "\"매칭을 찾을 수 없습니다.\"")))
    })
    @PatchMapping("/{matchingId}/confirm")
    public ResponseEntity<MatchingResponseDto> confirmCompletion(@PathVariable Long matchingId) {
        log.info("[PATCH] 작업 완료 승인 요청: id={}", matchingId);
        return ResponseEntity.ok(matchingService.confirmCompletion(matchingId));
    }

    /**
     * 매칭 상세 조회 API
     *
     * 클라이언트 또는 전문가가 매칭 ID를 통해 매칭 상세 정보를 조회합니다.
     * 견적 금액, 상태, 일정, 거절 사유 등을 포함한 정보를 제공합니다.
     *
     * @param matchingId 조회할 매칭 ID
     * @return 매칭 상세 정보 응답 DTO
     */
    @Operation(summary = "매칭 상세 조회", description = "매칭 ID를 통해 매칭 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MatchingResponseDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"matchingId\": 3,\n" +
                                    "  \"memberEmail\": \"client@example.com\",\n" +
                                    "  \"contentTitle\": \"EXAMPLE\",\n" +
                                    "  \"expertEmail\": \"expert@example.com\",\n" +
                                    "  \"expertId\": 10,\n" +
                                    "  \"status\": \"IN_PROGRESS\",\n" +
                                    "  \"startDate\": \"2025-07-25\",\n" +
                                    "  \"endDate\": \"2025-07-28\",\n" +
                                    "  \"rejectedReason\": null,\n" +
                                    "  \"totalPrice\": 300000,\n" +
                                    "  \"items\": [\n" +
                                    "    { \"name\": \"항목 1\", \"price\": 100000 },\n" +
                                    "    { \"name\": \"항목 2\", \"price\": 200000 }\n" +
                                    "  ]\n" +
                                    "}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "매칭 없음",
                    content = @Content(examples = @ExampleObject(value = "\"매칭을 찾을 수 없습니다.\"")))
    })
    @GetMapping("/{matchingId}")
    public ResponseEntity<MatchingResponseDto> getMatchingDetail(@PathVariable Long matchingId) {
        log.info("[GET] 매칭 상세 조회 요청: id={}", matchingId);
        return ResponseEntity.ok(matchingService.getMatchingDetail(matchingId));
    }
}