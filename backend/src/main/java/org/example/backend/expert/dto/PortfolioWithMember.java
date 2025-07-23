package org.example.backend.expert.dto;

import lombok.Data;
import org.example.backend.entity.Member;
import org.example.backend.entity.Portfolio;

@Data
public class PortfolioWithMember {
    private final Portfolio portfolio;
    private final Member member;
}
