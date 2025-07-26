package org.example.backend.repository;

import org.example.backend.entity.Review;
import org.example.backend.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReview(Review review);
}
