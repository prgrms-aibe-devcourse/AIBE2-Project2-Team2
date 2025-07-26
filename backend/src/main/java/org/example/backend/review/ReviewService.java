package org.example.backend.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.constant.MatchingStatus;
import org.example.backend.constant.Role;
import org.example.backend.entity.*;
import org.example.backend.exception.customException.MemberNotFoundException;
import org.example.backend.firebase.FirebaseImageService;
import org.example.backend.repository.MatchingRepository;
import org.example.backend.repository.MemberRepository;
import org.example.backend.repository.ReviewImageRepository;
import org.example.backend.repository.ReviewRepository;
import org.example.backend.review.dto.response.ReviewResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FirebaseImageService firebaseImageService;
    private final MemberRepository memberRepository;
    private final MatchingRepository matchingRepository;
    private final ReviewImageRepository reviewImageRepository;


    public void createReview(Long matchingId, String comment, Double rating, MultipartFile image, String email) {
        log.info("리뷰 생성 시작 - 매칭 ID: {}, 작성자 이메일: {}", matchingId, email);

        // 작성자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("작성자를 찾을 수 없습니다. 이메일: " + email));

        // 매칭 조회 및 검증
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 매칭을 찾을 수 없습니다. ID: " + matchingId));

        if (!matching.getStatus().equals(MatchingStatus.CONFIRMED)) {
            log.info("매칭 상태가 완료되지 않았습니다. 매칭 ID: {}", matchingId);
            throw new IllegalArgumentException("매칭 상태가 완료되지 않았습니다.");
        }

        if (!matching.getMember().getEmail().equals(email)) {
            log.info("작성자는 해당 매칭의 의뢰인이 아닙니다. 매칭 ID: {}, 작성자 이메일: {}", matchingId, email);
            throw new IllegalArgumentException("작성자는 해당 매칭의 의뢰인이 아닙니다.");
        }

        if (reviewRepository.existsByMatching_MatchingId(matchingId)) {
            log.info("이미 리뷰가 작성된 매칭입니다. 매칭 ID: {}", matchingId);
            throw new IllegalArgumentException("이미 리뷰가 작성된 매칭입니다.");
        }

        // 리뷰 엔티티 저장
        Review review = new Review(matching, rating, comment);
        reviewRepository.save(review);

        // 이미지 업로드 및 ReviewImage 저장
        if (image != null && !image.isEmpty()) {
            String imageUrl = firebaseImageService.uploadImage(image, "review/" + matchingId + "_" + member.getEmail());
            log.info("리뷰 이미지 업로드 완료 - 이미지 URL: {}", imageUrl);

            ReviewImage reviewImage = new ReviewImage(review, imageUrl);
            reviewImageRepository.save(reviewImage);
        }

        // 리뷰 수 및 평점 업데이트
        ExpertProfile expertProfile = matching.getContent().getMember().getExpertProfile();
        if (expertProfile != null) {
            expertProfile.addRating(rating);
        }
    }

    public ReviewResponseDto getReviewsByMemberId(Long memberId, Pageable pageable) {
        log.info("리뷰 조회 시작 - 회원 ID: {}", memberId);

        // 회원 조회 + 전문가 검증
        Member expert = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다. ID: " + memberId));

        if (!expert.getRole().equals(Role.EXPERT)) {
            log.info("해당 회원은 전문가가 아닙니다. 회원 ID: {}", memberId);
            throw new IllegalArgumentException("해당 회원은 전문가가 아닙니다.");
        }

        // ExpertProfile 존재 여부 확인
        ExpertProfile expertProfile = expert.getExpertProfile();
        if (expertProfile == null) {
            log.info("전문가 프로필이 존재하지 않습니다. 회원 ID: {}", memberId);
            throw new IllegalArgumentException("전문가 프로필이 존재하지 않습니다.");
        }

        // 전문가 정보에서 총 리뷰 수와 평점
        Long reviewCount = expertProfile.getReviewCount() != null ? expertProfile.getReviewCount() : 0L;
        Double averageRating = expertProfile.getRating() != null ? expertProfile.getRating() : 0.0;

        // 해당 전문가에 대한 리뷰 목록 페이징 조회
        Page<Review> reviewPage = reviewRepository.findReviewsByExpertMemberId(memberId, pageable);

        // Review -> ReviewDetailDto 변환
        Page<ReviewResponseDto.ReviewDetailDto> reviewDetailPage = reviewPage.map(this::convertToReviewDetailDto);

        log.info("리뷰 조회 완료 - 회원 ID: {}, 총 리뷰 수: {}, 현재 페이지 리뷰 수: {}",
                memberId, reviewCount, reviewDetailPage.getNumberOfElements());

        return new ReviewResponseDto(averageRating, reviewCount, reviewDetailPage);
    }

    /**
     * Review 엔티티를 ReviewDetailDto로 변환
     * - reviewer: 매칭의 member (리뷰를 작성한 일반 사용자)
     */
    private ReviewResponseDto.ReviewDetailDto convertToReviewDetailDto(Review review) {
        // 리뷰 작성자는 매칭의 member (의뢰인)
        Member reviewer = review.getMatching().getMember();

        return ReviewResponseDto.ReviewDetailDto.builder()
                .reviewId(review.getReviewId())
                .comment(review.getComment())
                .rating(review.getRating())
                .reviewerNickname(reviewer.getNickname())
                .reviewerProfileImageUrl(reviewer.getProfileImageUrl())
                .createdAt(review.getRegTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }

    @Transactional
    public void deleteReview(Long reviewId, String email) {
        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다. ID: " + reviewId));

        // 리뷰 평점 임시저장
        Double rating = review.getRating();

        // 작성자 검증
        if (!review.getMatching().getMember().getEmail().equals(email)) {
            log.info("작성자가 해당 리뷰의 작성자가 아닙니다. 리뷰 ID: {}, 작성자 이메일: {}", reviewId, email);
            throw new IllegalArgumentException("작성자가 해당 리뷰의 작성자가 아닙니다.");
        }

        // 리뷰 이미지가 존재하면 Firebase에서 삭제
        ReviewImage reviewImage = review.getReviewImage();
        if (reviewImage != null) {
            firebaseImageService.deleteImage(reviewImage.getImageUrl());
            // 참조도 끊어줘야 orphanRemoval 반영됨
            review.removeImage();
        }

        // 상태값을 DELETED로 변경 (소프트 삭제)
        review.markAsDeleted();

        // 리뷰 수 및 평점 업데이트
        ExpertProfile expertProfile = review.getMatching().getContent().getMember().getExpertProfile();
        if (expertProfile != null) {
            expertProfile.subRating(rating);
        }
    }

}
