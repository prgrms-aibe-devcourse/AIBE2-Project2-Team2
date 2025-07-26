package org.example.backend.review.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private byte rating;
    private String comment;
    private List<ReviewImageDto> images;
}
