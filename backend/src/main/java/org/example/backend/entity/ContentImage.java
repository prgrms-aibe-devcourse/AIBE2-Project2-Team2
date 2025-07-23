package org.example.backend.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "content_img")
public class ContentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    private String imageUrl;

    @Column(name = "order_index", nullable = false)
    private byte orderIndex;
}