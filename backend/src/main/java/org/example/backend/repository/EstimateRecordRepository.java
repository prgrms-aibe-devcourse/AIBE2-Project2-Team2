package org.example.backend.repository;

import org.example.backend.entity.EstimateRecord;
import org.example.backend.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstimateRecordRepository extends JpaRepository<EstimateRecord,Long> {
    Optional<EstimateRecord> findByMatching(Matching matching);
}
