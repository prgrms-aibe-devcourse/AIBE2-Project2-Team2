package org.example.backend.matchinghistory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.MatchingStatus;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchingSummaryDto {

    // 매칭 식별 정보
    private Long matchingId;

    // 콘텐츠 정보
    private String contentTitle;
    private String contentThumbnailUrl;

    // 전문가 정보
    private String expertName;
    private String expertProfileImageUrl;
    private String expertPhone;

    // 매칭 상태 및 작업 일정
    private MatchingStatus matchingStatus;
    private LocalDate workStartDate;
    private LocalDate workEndDate;

    // 견적서 총 금액
    private Integer totalPrice;

    // 고른 상품 목록
    private List<SelectedItemDto> selectedItems;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SelectedItemDto {
        private String itemName;
        private Integer itemPrice;
    }
}
