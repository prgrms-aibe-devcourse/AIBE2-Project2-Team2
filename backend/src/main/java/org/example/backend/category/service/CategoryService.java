package org.example.backend.category.service;

import lombok.RequiredArgsConstructor;

import org.example.backend.category.dto.CategoryTreeDto;
import org.example.backend.entity.Category;
import org.example.backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryTreeDto> getCategoryTree() {
        // 최상위 카테고리만 뽑고, 재귀로 children까지 DTO 변환
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(CategoryTreeDto::new)
                .collect(Collectors.toList());
    }
}