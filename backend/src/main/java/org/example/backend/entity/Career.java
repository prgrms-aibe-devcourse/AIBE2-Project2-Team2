package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "careers")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "career_id")
    private Long careerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expert_profile_id", nullable = false)
    private ExpertProfile expertProfile;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // 생성자에 expertProfile 포함
    public Career(String description, ExpertProfile expertProfile) {
        this.description = description;
        this.expertProfile = expertProfile;
    }

    // 또는 연관관계 편의 메서드
    public void assignExpertProfile(ExpertProfile expertProfile) {
        this.expertProfile = expertProfile;
    }

}
