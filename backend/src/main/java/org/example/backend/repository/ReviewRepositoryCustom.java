package org.example.backend.repository;

import org.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    Page<Review> findReviewsByExpertMemberId(Long expertMemberId, Pageable pageable);
}
