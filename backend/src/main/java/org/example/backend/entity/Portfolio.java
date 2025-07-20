package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "portfolios")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_profile_id", nullable = false)
    private ExpertProfile expertProfile;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name ="working_year", nullable = false)
    private Integer workingYear;

    @Column(name = "category", nullable = false)
    private String category;        //DetailField에 속한 내용으로 이름만 가져온다.

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "sort_order")
    private List<PortfolioImage> images = new ArrayList<>();

    public Portfolio(ExpertProfile expertProfile, String title, String content, Integer workingYear, String category) {
        this.expertProfile = expertProfile;
        this.title = title;
        this.content = content;
        this.viewCount = 0L; // 초기 조회수는 0
        this.workingYear = workingYear;
        this.category = category;
    }

}
