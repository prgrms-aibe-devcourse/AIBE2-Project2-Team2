package org.example.backend.review.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.review.dto.ReviewRequestDto;
import org.example.backend.review.dto.ReviewResponseDto;
import org.example.backend.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @ModelAttribute ReviewRequestDto reviewRequestDto
    ) {
        ReviewResponseDto response = reviewService.createReview(reviewRequestDto);
        return ResponseEntity.ok(response);
    }
}