package org.example.backend.repository;

import org.example.backend.constant.Role;
import org.example.backend.constant.Status;
import org.example.backend.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Status status);

    @Query("SELECT c FROM Content c " +
            "JOIN FETCH c.member m " +
            "JOIN FETCH m.expertProfile ep " +
            "WHERE c.contentId = :contentId AND m.role = :role")
    Optional<Content> findByIdWithExpertProfile(@Param("contentId") Long contentId, @Param("role") Role role);

    Page<Content> findByCategory_CategoryIdAndStatus(Long categoryId, Status status, Pageable pageable);
    List<Content> findByCategory_CategoryIdAndStatus(Long categoryId, Status status);
}
