package org.example.backend.repository;

import org.example.backend.entity.Portfolio;
import org.example.backend.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
    List<PortfolioImage> findByPortfolio(Portfolio portfolio);
}