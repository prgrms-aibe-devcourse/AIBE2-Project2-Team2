package org.example.backend.category.service;

import lombok.RequiredArgsConstructor;

import org.example.backend.category.dto.CategoryTreeDto;
import org.example.backend.entity.Category;
import org.example.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryTreeDto> getCategoryTree() {
        // 최상위 카테고리만 뽑고, 재귀로 children까지 DTO 변환
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(CategoryTreeDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리 하위의 최하위(leaf) 카테고리 ID들만 수집
     * 컨텐츠는 항상 최하위 카테고리에만 연결되므로, 중간 노드는 제외
     * @param categoryId 상위 카테고리 ID
     * @return 해당 카테고리 하위의 최하위 카테고리 ID 리스트
     */
    public List<Long> getAllSubCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();

        // 해당 카테고리가 존재하는지 확인
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리 ID: " + categoryId);
        }

        collectLeafCategoryIds(categoryId, result);
        return result;
    }

    /**
     * 재귀적으로 최하위(leaf) 카테고리 ID들만 수집하는 내부 메서드
     * @param categoryId 현재 카테고리 ID
     * @param result 수집된 최하위 카테고리 ID들을 저장할 리스트
     */
    private void collectLeafCategoryIds(Long categoryId, List<Long> result) {
        // 현재 카테고리의 하위 카테고리들 조회
        List<Category> children = categoryRepository.findByParentCategoryId(categoryId);

        if (children.isEmpty()) {
            // 하위 카테고리가 없으면 최하위 카테고리이므로 결과에 추가
            result.add(categoryId);
        } else {
            // 하위 카테고리가 있으면 각각에 대해 재귀 호출
            for (Category child : children) {
                collectLeafCategoryIds(child.getCategoryId(), result);
            }
        }
    }
}