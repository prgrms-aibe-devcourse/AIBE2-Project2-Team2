package org.example.backend.matchinghistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.constant.MatchingStatus;

import java.time.YearMonth;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchingSearchCondition {

    @Schema(
            description = "매칭 상태 필터링. " +
                    "ACCEPTED: 작업 진행 전, " +
                    "IN_PROGRESS: 작업 진행 중, " +
                    "WORK_COMPLETED: 유저 컨펌 필요, " +
                    "CONFIRMED: 작업 최종 완료, " +
                    "REJECTED: 매칭 거절, " +
                    "CANCELLED: 매칭 취소",
            example = "ACCEPTED"
    )
    private MatchingStatus matchingStatus;

    @Schema(description = "검색 시작 월 (YYYY-MM)", example = "2024-01")
    private YearMonth fromMonth;

    @Schema(description = "검색 종료 월 (YYYY-MM)", example = "2025-12")
    private YearMonth toMonth;

    @Schema(description = "매칭 상대방 닉네임 포함 검색", example = "tester")
    private String nickname;

    @Schema(description = "특정 매칭 ID 검색", example = "1")
    private Long matchingId;
}
