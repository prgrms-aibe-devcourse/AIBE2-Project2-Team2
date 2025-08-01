package org.example.backend.repository;

import org.example.backend.entity.ExpertProfile;
import org.example.backend.entity.ExpertProfileSpecialtyDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpertProfileSpecialtyDetailRepository extends JpaRepository<ExpertProfileSpecialtyDetail, Long> {
    void deleteAllByExpertProfile(ExpertProfile profile);
}