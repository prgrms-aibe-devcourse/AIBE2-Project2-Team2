package org.example.backend.repository;

import org.example.backend.entity.ContentImage;
import org.example.backend.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {
    List<ContentImage> findAllByContent(Content content);
} 