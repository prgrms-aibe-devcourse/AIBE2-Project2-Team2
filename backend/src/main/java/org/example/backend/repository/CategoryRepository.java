package org.example.backend.repository;

import org.example.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 최상위 카테고리만 조회
    List<Category> findByParentIsNull();

    /**
     * 특정 부모 카테고리의 직접적인 하위 카테고리들을 조회
     * @param parentId 부모 카테고리 ID
     * @return 하위 카테고리 리스트
     */
    List<Category> findByParentCategoryId(Long parentId);
}
