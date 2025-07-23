package org.example.backend.repository;

import org.example.backend.entity.Member;
import org.example.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByMatching_Member(Member member);
}
