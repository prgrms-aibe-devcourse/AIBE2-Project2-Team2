package org.example.backend.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Boolean isRead;
    private LocalDateTime sendAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, String message) {
        this.chatRoom = chatRoom;
        this.message = message;
        this.isRead = false;
        this.sendAt = LocalDateTime.now();
    }
}
