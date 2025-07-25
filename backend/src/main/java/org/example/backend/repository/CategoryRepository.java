package org.example.backend.repository;

import org.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 최상위 카테고리만 조회
    List<Category> findByParentIsNull();
}
