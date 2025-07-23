package org.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.constant.Status;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "content")
@NoArgsConstructor
public class Content extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_Id", nullable = false)
    private Member member;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Long budget;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(nullable = false)
    private String category;

    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Matching> matchingList = new ArrayList<>();

    // Status 설정 메서드
    public void setStatus(Status status) {
        this.status = status;
    }

    // Content 수정 메서드
    public void updateContent(String title, String description, Long budget, String category) {
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.category = category;
    }
}
