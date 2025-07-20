package org.example.backend.repository;

import org.example.backend.entity.Skill;
import org.example.backend.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    Optional<Skill> findByNameAndCategory(String name, SkillCategory category);
}