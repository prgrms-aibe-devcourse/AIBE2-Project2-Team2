package org.example.backend.matching.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.MatchingStatus;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
public class MatchingStatusUpdateDto {

    @NotNull(message = "변경할 상태값은 필수입니다.")
    private MatchingStatus status;
}
