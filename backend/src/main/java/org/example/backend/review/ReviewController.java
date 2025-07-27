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
import org.example.backend.review.dto.request.CreateReviewRequest;
import org.example.backend.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
    @PostMapping(value = "/{matchingId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReview(
            @PathVariable Long matchingId,
            @ModelAttribute @Valid CreateReviewRequest reviewRequest,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Principal principal
    ) {
        // 기존 로직 그대로
        reviewService.createReview(
                matchingId,
                reviewRequest.getComment(),
                reviewRequest.getRating(),
                image,
                principal.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("리뷰가 성공적으로 작성되었습니다.");
    }


    // 리뷰 조회 API
    @Operation(
            summary = "특정 컨텐츠의 전문가 리뷰 조회",
            description = "컨텐츠 ID를 통해 해당 전문가에 대한 리뷰 목록을 페이징 처리하여 조회합니다. 최신순 정렬이며, 상태값이 ACTIVE인 리뷰만 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리뷰 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponseDto.class),
                            examples = @ExampleObject(value = "{"
                                    + "\"totalRating\": 4.5,"
                                    + "\"totalReviewCount\": 15,"
                                    + "\"reviews\": {"
                                    +   "\"content\": ["
                                    +     "{"
                                    +       "\"reviewId\": 101,"
                                    +       "\"comment\": \"정말 친절하고 전문적이셨어요! 다음에도 이용하고 싶습니다.\","
                                    +       "\"rating\": 5.0,"
                                    +       "\"reviewerNickname\": \"happyClient\","
                                    +       "\"reviewerProfileImageUrl\": \"https://firebasestorage.googleapis.com/profile1.jpg\","
                                    +       "\"reviewImageUrl\": \"https://firebasestorage.googleapis.com/review1.jpg\","
                                    +       "\"createdAt\": \"2025-07-26 14:30\""
                                    +     "},"
                                    +     "{"
                                    +       "\"reviewId\": 102,"
                                    +       "\"comment\": \"서비스는 좋았지만 약속 시간보다 조금 늦으셨어요.\","
                                    +       "\"rating\": 4.0,"
                                    +       "\"reviewerNickname\": \"normalUser\","
                                    +       "\"reviewerProfileImageUrl\": \"https://firebasestorage.googleapis.com/profile2.jpg\","
                                    +       "\"reviewImageUrl\": null,"
                                    +       "\"createdAt\": \"2025-07-25 16:45\""
                                    +     "}"
                                    +   "],"
                                    +   "\"pageable\": {"
                                    +     "\"pageNumber\": 0,"
                                    +     "\"pageSize\": 5,"
                                    +     "\"sort\": {"
                                    +       "\"sorted\": true,"
                                    +       "\"direction\": \"DESC\","
                                    +       "\"property\": \"regTime\""
                                    +     "}"
                                    +   "},"
                                    +   "\"totalPages\": 3,"
                                    +   "\"totalElements\": 15,"
                                    +   "\"last\": false,"
                                    +   "\"size\": 5,"
                                    +   "\"number\": 0,"
                                    +   "\"first\": true,"
                                    +   "\"numberOfElements\": 5,"
                                    +   "\"empty\": false"
                                    + "}"
                                    + "}")
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 컨텐츠 ID)", content = @Content),
            @ApiResponse(responseCode = "404", description = "컨텐츠를 찾을 수 없거나 해당 멤버가 전문가가 아님", content = @Content)
    })
    @GetMapping("/{contentId}")
    public ResponseEntity<ReviewResponseDto> getReviewsByContentId(
            @Parameter(description = "컨텐츠 ID", required = true, example = "123")
            @PathVariable Long contentId,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "5")
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("Fetching reviews for contentId: {}", contentId); // 수정됨

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "regTime"));
        ReviewResponseDto reviews = reviewService.getReviewsByContentId(contentId, pageable);
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
