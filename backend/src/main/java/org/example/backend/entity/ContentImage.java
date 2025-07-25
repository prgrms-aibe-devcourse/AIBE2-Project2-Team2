package org.example.backend.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
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

    @Column(name = "thumbnail", nullable = false)
    private boolean thumbnail = false;

    public ContentImage(Content content, String imageUrl, byte orderIndex) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.orderIndex = orderIndex;
    }

    public void setThumbnail(boolean thumbnail) {
        this.thumbnail = thumbnail;
    }
}