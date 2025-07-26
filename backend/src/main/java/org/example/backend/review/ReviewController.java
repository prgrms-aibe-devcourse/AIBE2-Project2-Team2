package org.example.backend.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성 API
    @Operation(
            summary = "리뷰 생성",
            description = "특정 매칭에 대한 리뷰를 작성합니다. 리뷰 내용과 평점, 이미지 파일을 함께 업로드합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리뷰가 성공적으로 작성되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 유효하지 않은 입력값)", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 매칭을 찾을 수 없음", content = @Content)
    })
    @PostMapping("/{matchingId}")
    public ResponseEntity<?> createReview(
            @Parameter(description = "리뷰 대상 매칭 ID", required = true, example = "123")
            @PathVariable Long matchingId,

            @Parameter(description = "리뷰 내용 최대 200자", required = true, example = "매우 만족스러운 작업이었습니다. 디테일이 뛰어나고, 소통도 원활했습니다.")
            @RequestParam("comment") String comment,

            @Parameter(description = "리뷰 평점 (1~5)", required = true, example = "5.0")
            @RequestParam(value = "rating", defaultValue = "5.0") Double rating,

            @Parameter(description = "리뷰 이미지 파일 (썸네일 제외)", required = true)
            @RequestPart("images") MultipartFile image,

            Principal principal
    ) {
        log.info("Creating review for matchingId: {}", matchingId);
        String email = principal.getName();
        reviewService.createReview(matchingId, comment, rating, image, email);
        return ResponseEntity.status(HttpStatus.CREATED).body("리뷰가 성공적으로 작성되었습니다.");
    }


    // 리뷰 조회 API
    @Operation(summary = "특정 전문가의 리뷰 조회", description = "전문가(memberId)에 대한 리뷰 목록을 페이징 처리하여 조회합니다. 최신순 정렬이며, 상태값이 ACTIVE인 리뷰만 반환됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리뷰 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponseDto.class),
                            examples = @ExampleObject(value = "{"
                                    + "\"totalRating\": 4.5,"
                                    + "\"totalReviewCount\": 2,"
                                    + "\"reviews\": {"
                                    +   "\"content\": ["
                                    +     "{"
                                    +       "\"reviewId\": 101,"
                                    +       "\"comment\": \"정말 친절하셨어요!\","
                                    +       "\"rating\": 5.0,"
                                    +       "\"reviewerNickname\": \"happyUser\","
                                    +       "\"reviewerProfileImageUrl\": \"https://example.com/profile1.jpg\","
                                    +       "\"createdAt\": \"2025-07-24 13:22\""
                                    +     "},"
                                    +     "{"
                                    +       "\"reviewId\": 102,"
                                    +       "\"comment\": \"좋았지만 조금 늦었어요.\","
                                    +       "\"rating\": 4.0,"
                                    +       "\"reviewerNickname\": \"slowGuy\","
                                    +       "\"reviewerProfileImageUrl\": \"https://example.com/profile2.jpg\","
                                    +       "\"createdAt\": \"2025-07-23 16:45\""
                                    +     "}"
                                    +   "],"
                                    +   "\"pageable\": {"
                                    +     "\"pageNumber\": 0,"
                                    +     "\"pageSize\": 5"
                                    +   "},"
                                    +   "\"totalPages\": 1,"
                                    +   "\"totalElements\": 2,"
                                    +   "\"last\": true,"
                                    +   "\"size\": 5,"
                                    +   "\"number\": 0,"
                                    +   "\"sort\": {"
                                    +     "\"sorted\": true,"
                                    +     "\"unsorted\": false,"
                                    +     "\"empty\": false"
                                    +   "},"
                                    +   "\"first\": true,"
                                    +   "\"numberOfElements\": 2,"
                                    +   "\"empty\": false"
                                    + "}"
                                    + "}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "404", description = "리뷰 또는 전문가를 찾을 수 없음", content = @Content)
    })
    @GetMapping("/{memberId}")
    public ResponseEntity<ReviewResponseDto> getReviewsByMemberId(
            @PathVariable Long memberId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("Fetching reviews for memberId: {}", memberId);

        // BaseTimeEntity의 실제 필드명인 regTime으로 정렬
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "regTime"));

        ReviewResponseDto reviews = reviewService.getReviewsByMemberId(memberId, pageable);
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 삭제 API
    @Operation(
            summary = "리뷰 삭제",
            description = "리뷰 ID에 해당하는 리뷰를 삭제합니다. 작성자만 삭제할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "리뷰가 성공적으로 삭제되었습니다."),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없음", content = @Content)
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Principal principal
    ) {
        log.info("Deleting review with reviewId: {}", reviewId);
        String email = principal.getName();
        reviewService.deleteReview(reviewId, email);
        return ResponseEntity.noContent().build();
    }
}
