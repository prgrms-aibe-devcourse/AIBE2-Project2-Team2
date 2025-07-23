package org.example.backend.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponseDto {

    private Long paymentId;
    private Long amount;
    private LocalDateTime paymentDate;
    private String status;

    private Long contentId;
    private String contentTitle;

    private String expertName;
    private String expertProfileImageUrl;
}
