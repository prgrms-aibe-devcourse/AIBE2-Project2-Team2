package org.example.backend.repository;

import org.example.backend.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
}