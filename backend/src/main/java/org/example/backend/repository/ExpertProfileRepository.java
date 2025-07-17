package org.example.backend.repository;

import org.example.backend.entity.ExpertProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertProfileRepository extends JpaRepository<ExpertProfile, Long> {
}