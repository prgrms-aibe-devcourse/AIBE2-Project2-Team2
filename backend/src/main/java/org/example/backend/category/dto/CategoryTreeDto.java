package org.example.backend.category.dto;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryTreeDto {
    private Long id;
    private String name;
    private List<CategoryTreeDto> children;

    public CategoryTreeDto(org.example.backend.entity.Category category) {
        this.id = category.getCategoryId();
        this.name = category.getName();
        this.children = category.getChildren().stream()
                .map(CategoryTreeDto::new)
                .collect(Collectors.toList());
    }

    public CategoryTreeDto(Long id, String name, List<CategoryTreeDto> children) {
        this.id = id;
        this.name = name;
        this.children = children;
    }
}