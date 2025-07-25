package org.example.backend.repository;

import org.example.backend.constant.Status;
import org.example.backend.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByStatus(Status status);
}
