package org.example.backend.review.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.*;
import org.example.backend.firebase.FirebaseImageService;
import org.example.backend.repository.*;
import org.example.backend.review.dto.ReviewImageDto;
import org.example.backend.review.dto.ReviewRequestDto;
import org.example.backend.review.dto.ReviewResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int MAX_IMAGES = 5;

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final MatchingRepository matchingRepository;
    private final MemberRepository memberRepository;
    private final ContentRepository contentRepository;
    private final FirebaseImageService firebaseImageService;

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto dto) {
        log.info("리뷰 생성 요청 수신됨: reviewerId={}, revieweeId={}, matchingId={}",
                dto.getReviewerId(), dto.getRevieweeId(), dto.getMatchingId());

        if (dto.getImages() != null && dto.getImages().size() > MAX_IMAGES) {
            log.warn("리뷰 이미지 업로드 개수 초과: {}장", dto.getImages().size());
            throw new IllegalArgumentException("최대 " + MAX_IMAGES + "장의 이미지만 업로드 가능합니다.");
        }

        // Matching 객체를 찾는 부분 수정 (이미 데이터베이스에서 찾고 있음)
        Matching matching = matchingRepository.findById(dto.getMatchingId())
                .orElseThrow(() -> {
                    log.error("매칭 정보 없음: id={}", dto.getMatchingId());
                    return new IllegalArgumentException("매칭 정보 없음");
                });

        // Member 객체 찾아오기
        Member reviewer = memberRepository.findById(dto.getReviewerId())
                .orElseThrow(() -> {
                    log.error("리뷰 작성자 정보 없음: id={}", dto.getReviewerId());
                    return new IllegalArgumentException("리뷰 작성자 없음");
                });

        Member reviewee = memberRepository.findById(dto.getRevieweeId())
                .orElseThrow(() -> {
                    log.error("리뷰 대상자 정보 없음: id={}", dto.getRevieweeId());
                    return new IllegalArgumentException("리뷰 대상자 없음");
                });

        // Review 객체 생성 및 설정
        Review review = new Review();
        review.setMatching(matching);
        review.setMember(reviewer);
        review.setReviewer(reviewer);
        review.setReviewee(reviewee);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 저장 완료: reviewId={}", savedReview.getId());

        // 이미지 관련 처리
        List<ReviewImageDto> imageDtos = new ArrayList<>();
        if (dto.getImages() != null) {
            String nickname = reviewer.getNickname() != null ? reviewer.getNickname() : "anonymous";

            List<Integer> imageOrders = dto.getImageOrders();

            for (int i = 0; i < dto.getImages().size(); i++) {
                MultipartFile file = dto.getImages().get(i);
//                int orderIndex = (imageOrders != null && imageOrders.size() > i) ? imageOrders.get(i) : i;
                int orderIndex = i; // 기본값

                if (imageOrders != null) {
                    try {
                        orderIndex = imageOrders.get(i);
                    } catch (IndexOutOfBoundsException e) {
                        log.warn("imageOrders 인덱스 초과: i={}, imageOrders.size={}", i, imageOrders.size());
                    }
                }

                String fileName = "review/" + nickname + "_" + savedReview.getId() + "_img_" + i;
                String imageUrl = firebaseImageService.uploadImage(file, fileName);

                ReviewImage img = new ReviewImage();
                img.setReview(savedReview);
                img.setImageUrl(imageUrl);
                img.setOrderIndex((byte) orderIndex);

                reviewImageRepository.save(img);
                imageDtos.add(new ReviewImageDto(imageUrl, (byte) orderIndex));
            }
        }

        log.info("리뷰 생성 프로세스 완료: reviewId={}", savedReview.getId());
        return new ReviewResponseDto(savedReview.getId(), savedReview.getRating(), savedReview.getComment(), imageDtos);
    }


    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto dto) {
        log.info("리뷰 수정 요청 수신됨: reviewId={}, reviewerId={}", reviewId, dto.getReviewerId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰 없음: reviewId={}", reviewId);
                    return new IllegalArgumentException("리뷰가 존재하지 않습니다.");
                });

        // 작성자 권한 체크
        if (!review.getReviewer().getMemberId().equals(dto.getReviewerId())) {
            log.warn("권한 없음: reviewId={}, 요청자={}", reviewId, dto.getReviewerId());
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }

        if (dto.getImages() != null && dto.getImages().size() > MAX_IMAGES) {
            log.warn("리뷰 이미지 업로드 개수 초과: {}장", dto.getImages().size());
            throw new IllegalArgumentException("최대 " + MAX_IMAGES + "장의 이미지만 업로드 가능합니다.");
        }

        // 리뷰 내용 수정
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        // 기존 이미지 삭제
        List<ReviewImage> oldImages = reviewImageRepository.findByReview(review);
        for (ReviewImage img : oldImages) {
            firebaseImageService.deleteImage(img.getImageUrl());
            reviewImageRepository.delete(img);
        }

        List<ReviewImageDto> imageDtos = new ArrayList<>();
        if (dto.getImages() != null) {
            String nickname = review.getReviewer().getNickname() != null ? review.getReviewer().getNickname() : "anonymous";
            List<Integer> imageOrders = dto.getImageOrders();

            for (int i = 0; i < dto.getImages().size(); i++) {
                MultipartFile file = dto.getImages().get(i);
                int orderIndex = (imageOrders != null && imageOrders.size() > i) ? imageOrders.get(i) : i;

                String fileName = "review/" + nickname + "_" + review.getId() + "_img_" + i;
                String imageUrl = firebaseImageService.uploadImage(file, fileName);

                ReviewImage img = new ReviewImage();
                img.setReview(review);
                img.setImageUrl(imageUrl);
                img.setOrderIndex((byte) orderIndex);

                reviewImageRepository.save(img);
                imageDtos.add(new ReviewImageDto(imageUrl, (byte) orderIndex));
            }
        }

        Review updatedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료: reviewId={}", updatedReview.getId());

        return new ReviewResponseDto(updatedReview.getId(), updatedReview.getRating(), updatedReview.getComment(), imageDtos);
    }


    @Transactional
    public void deleteReview(Long reviewId, Long reviewerId) {
        log.info("리뷰 삭제 요청 수신됨: reviewId={}, reviewerId={}", reviewId, reviewerId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.error("리뷰 없음: reviewId={}", reviewId);
                    return new IllegalArgumentException("리뷰가 존재하지 않습니다.");
                });

        // 작성자 권한 체크
        if (!review.getReviewer().getMemberId().equals(reviewerId)) {
            log.warn("권한 없음: reviewId={}, 요청자={}", reviewId, reviewerId);
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다.");
        }

        // 이미지 삭제 (Firebase + DB)
        List<ReviewImage> images = reviewImageRepository.findByReview(review);
        for (ReviewImage img : images) {
//            firebaseImageService.deleteImage(img.getImageUrl());  // 여기 수정!
//            reviewImageRepository.delete(img);
            try {
                firebaseImageService.deleteImage(img.getImageUrl());
                reviewImageRepository.delete(img);
            } catch (Exception e) {
                log.error("이미지 삭제 실패: imageUrl={}, error={}", img.getImageUrl(), e.getMessage(), e);
                throw new RuntimeException("이미지 삭제 중 오류 발생");
            }
        }

        // 리뷰 삭제
        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료: reviewId={}", reviewId);
    }
}
