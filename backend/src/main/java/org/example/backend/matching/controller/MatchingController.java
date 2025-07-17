package org.example.backend.matching.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matchings")
@Tag(name = "Matching", description = "클라이언트와 전문가 간 매칭 기능 API")
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 매칭 요청 생성 API
     *
     * @param requestDto 매칭 요청에 필요한 contentId, clientId, estimateUrl, message 등을 포함한 DTO
     * @return 생성된 매칭 정보
     */
    @Operation(summary = "매칭 요청 생성", description = "클라이언트가 전문가의 콘텐츠에 대해 매칭을 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매칭 생성 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponseDto.class),
                    examples = @ExampleObject(value = "{ \"matchingId\": 1, \"contentId\": 1, \"clientId\": 1, \"status\": \"REQUESTED\", \"estimateUrl\": \"https://example.com\" }")
            )),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 사용자 또는 콘텐츠", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"존재하지 않는 사용자입니다.\"")
            )),
            @ApiResponse(responseCode = "409", description = "중복 매칭 요청", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"이미 해당 콘텐츠에 매칭 요청을 보냈습니다.\"")
            ))
    })
    @PostMapping
    public ResponseEntity<MatchingResponseDto> createMatching(@Valid @RequestBody MatchingRequestDto requestDto) {
        log.info("매칭 요청 생성 요청 받음");
        MatchingResponseDto response = matchingService.createMatching(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 매칭 상태 변경 API (ACCEPTED / REJECTED)
     *
     * @param matchingId 매칭 ID
     * @param statusDto 변경할 상태 DTO
     * @return 변경된 매칭 정보
     */
    @Operation(summary = "매칭 상태 변경", description = "전문가가 매칭 요청을 수락 또는 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponseDto.class),
                    examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"ACCEPTED\" }")
            )),
            @ApiResponse(responseCode = "404", description = "해당 매칭 없음", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"해당 매칭을 찾을 수 없습니다.\"")
            ))
    })
    @PatchMapping("/{matchingId}/status")
    public ResponseEntity<MatchingResponseDto> updateMatchingStatus(
            @PathVariable Long matchingId,
            @RequestBody MatchingStatusUpdateDto statusDto
    ) {
        log.info("매칭 상태 변경 요청 - ID: {}, 상태: {}", matchingId, statusDto.getStatus());
        MatchingResponseDto updated = matchingService.updateMatchingStatus(matchingId, statusDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * 작업 시작 API (PAID → IN_PROGRESS)
     *
     * @param matchingId 매칭 ID
     * @return 작업 시작된 매칭 정보
     */
    @Operation(summary = "작업 시작", description = "전문가가 결제 완료된 매칭 작업을 시작합니다. (PAID → IN_PROGRESS)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작업 시작 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponseDto.class),
                    examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"IN_PROGRESS\" }")
            )),
            @ApiResponse(responseCode = "400", description = "작업 시작 불가", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"결제 완료 상태에서만 작업을 시작할 수 있습니다.\"")
            ))
    })
    @PatchMapping("/{matchingId}/start-work")
    public ResponseEntity<MatchingResponseDto> startWork(@PathVariable Long matchingId) {
        log.info("작업 시작 요청 - matchingId: {}", matchingId);
        MatchingResponseDto updated = matchingService.startWork(matchingId);
        return ResponseEntity.ok(updated);
    }

    /**
     * 작업 완료 API (IN_PROGRESS → WORK_COMPLETED)
     *
     * @param matchingId 매칭 ID
     * @return 작업 완료된 매칭 정보
     */
    @Operation(summary = "작업 완료 처리", description = "전문가가 작업을 완료합니다. (IN_PROGRESS → WORK_COMPLETED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작업 완료 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponseDto.class),
                    examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"WORK_COMPLETED\" }")
            )),
            @ApiResponse(responseCode = "400", description = "작업 완료 불가", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"작업 완료 처리는 IN_PROGRESS 상태에서만 가능합니다.\"")
            ))
    })
    @PatchMapping("/{matchingId}/complete")
    public ResponseEntity<MatchingResponseDto> completeWork(@PathVariable Long matchingId) {
        log.info("작업 완료 요청 - matchingId: {}", matchingId);
        MatchingResponseDto updated = matchingService.completeWork(matchingId);
        return ResponseEntity.ok(updated);
    }

    /**
     * 완료 수락 API (WORK_COMPLETED → CONFIRMED)
     *
     * @param matchingId 매칭 ID
     * @return 매칭 완료 처리된 정보
     */
    @Operation(summary = "작업 완료 수락", description = "클라이언트가 작업 완료를 수락합니다. (WORK_COMPLETED → CONFIRMED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "완료 수락 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MatchingResponseDto.class),
                    examples = @ExampleObject(value = "{ \"matchingId\": 1, \"status\": \"CONFIRMED\" }")
            )),
            @ApiResponse(responseCode = "400", description = "완료 수락 불가", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "\"CONFIRMED 처리는 WORK_COMPLETED 상태에서만 가능합니다.\"")
            ))
    })
    @PatchMapping("/{matchingId}/confirm")
    public ResponseEntity<MatchingResponseDto> confirmCompletion(@PathVariable Long matchingId) {
        log.info("작업 완료 수락 요청 - matchingId: {}", matchingId);
        MatchingResponseDto updated = matchingService.confirmCompletion(matchingId);
        return ResponseEntity.ok(updated);
    }
}
