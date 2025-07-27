package org.example.backend.repository;

import org.example.backend.constant.Status;
import org.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    boolean existsByMatching_MatchingId(Long matchingId);
}
