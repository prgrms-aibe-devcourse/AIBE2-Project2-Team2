package org.example.backend.repository;

import org.example.backend.entity.DetailField;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailFieldRepository extends JpaRepository<DetailField, Long> {
}
