package org.example.backend.review.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Long matchingId;
    private Long reviewerId;
    private Long revieweeId;
    private byte rating;
    private String comment;
    private List<MultipartFile> images; // 최대 5장까지
    private List<Integer> imageOrders;
}
