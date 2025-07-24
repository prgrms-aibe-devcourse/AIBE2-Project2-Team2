package org.example.backend.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.backend.constant.MatchingStatus;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class MatchingStatusUpdateDto {

    @NotNull(message = "변경할 매칭 상태는 필수입니다.")
    private MatchingStatus status;

    private String reason; // REJECTED 혹은 CANCELLED 시 사용되는 사유 (선택)
}
