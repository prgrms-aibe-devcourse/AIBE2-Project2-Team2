package org.example.backend.repository;

import org.example.backend.entity.ExpertProfile;
import org.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertProfileRepository extends JpaRepository<ExpertProfile, Long>, ExpertProfileRepositoryCustom {
    Optional<ExpertProfile> findByMember(Member member);

}