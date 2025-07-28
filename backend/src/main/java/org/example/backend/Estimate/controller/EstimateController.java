package org.example.backend.Estimate.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.Estimate.dto.EstimateRecordResponseDto;
import org.example.backend.Estimate.dto.EstimateRequestDto;
import org.example.backend.Estimate.service.EstimateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class EstimateController {
    private final EstimateService estimateService;

    @PostMapping("/{matchingId}/estimate")
    public ResponseEntity<EstimateRecordResponseDto> submitEstimate(
            @PathVariable Long matchingId,
            @RequestBody EstimateRequestDto requestDto) {

        // 인증유저 추출 - JWT/SecurityContextHolder 방식
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        EstimateRecordResponseDto response = estimateService.createEstimate(matchingId, requestDto.getSelectedOptionIds(), email);
        return ResponseEntity.ok(response);
    }
}