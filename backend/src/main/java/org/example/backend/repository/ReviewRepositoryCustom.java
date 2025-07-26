package org.example.backend.repository;

import org.example.backend.constant.Status;
import org.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    Page<Review> findReviewsByExpertMemberId(Long expertMemberId, Pageable pageable);
    // 새로 추가된 메서드
    Page<Review> findReviewsByExpertMemberIdWithDetails(Long expertMemberId, Status status, Pageable pageable);
}
