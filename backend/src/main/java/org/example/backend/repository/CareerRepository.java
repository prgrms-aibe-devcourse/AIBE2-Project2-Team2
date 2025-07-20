package org.example.backend.repository;

import org.example.backend.entity.Career;
import org.example.backend.entity.ExpertProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, Long> {
    void deleteAllByExpertProfile(ExpertProfile profile);
}