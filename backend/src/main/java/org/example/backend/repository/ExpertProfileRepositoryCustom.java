package org.example.backend.repository;

import org.example.backend.expert.dto.response.ExpertProfileDto;

public interface ExpertProfileRepositoryCustom {
    ExpertProfileDto findExpertProfileByEmail(String email);
}
