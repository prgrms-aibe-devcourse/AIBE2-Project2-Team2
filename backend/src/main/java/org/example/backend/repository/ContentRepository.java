package org.example.backend.repository;

import org.example.backend.constant.Status;
import org.example.backend.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Status status);
    Page<Content> findByCategory_CategoryIdAndStatus(Long categoryId, Status status, Pageable pageable);
    List<Content> findByCategory_CategoryIdAndStatus(Long categoryId, Status status);
}
