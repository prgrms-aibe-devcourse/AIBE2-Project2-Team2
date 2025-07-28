package org.example.backend.matchinghistory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingSummaryUserDto extends MatchingSummaryBaseDto{

    // 일반유저 정보
    private String userName;
    private String userPhone;

}
