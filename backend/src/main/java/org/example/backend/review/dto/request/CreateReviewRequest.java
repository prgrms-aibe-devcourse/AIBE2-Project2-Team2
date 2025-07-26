package org.example.backend.review.dto.request;

import lombok.Data;

import javax.validation.constraints.*;

@Data
public class CreateReviewRequest {
    // getters and setters
    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(max = 200, message = "리뷰 내용은 최대 200자까지 입력 가능합니다")
    private String comment;

    @NotNull(message = "평점은 필수입니다")
    @DecimalMin(value = "1.0", message = "평점은 1.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "평점은 5.0 이하여야 합니다")
    private Double rating;
}
