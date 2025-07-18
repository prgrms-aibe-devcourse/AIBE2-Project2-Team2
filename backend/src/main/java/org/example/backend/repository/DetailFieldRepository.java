package org.example.backend.repository;

import org.example.backend.entity.DetailField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DetailFieldRepository extends JpaRepository<DetailField, Long> {
    Optional<DetailField> findByName(String name);
}
