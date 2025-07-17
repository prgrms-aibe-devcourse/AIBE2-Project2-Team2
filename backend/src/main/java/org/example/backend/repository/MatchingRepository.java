package org.example.backend.repository;

import org.example.backend.entity.Content;
import org.example.backend.entity.Matching;
import org.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    boolean existsByMemberAndContent(Member member, Content content);
}
