package org.example.backend.matchinghistory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.example.backend.constant.MatchingStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingSummaryExpertDto extends  MatchingSummaryBaseDto {

    // 전문가 정보
    private String expertName;
    private String expertPhone;

    private boolean reviewed;
}
