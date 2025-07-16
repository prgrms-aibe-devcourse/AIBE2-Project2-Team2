package org.example.backend.entity;

import lombok.Getter;
import org.example.backend.constant.Status;
import org.threeten.bp.LocalDateTime;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "content")
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
}
