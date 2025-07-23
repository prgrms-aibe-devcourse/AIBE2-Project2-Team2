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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Boolean isRead;
    private LocalDateTime sendAt;

    @Builder
    public ChatMessage(ChatRoom chatRoom, String message, Member sender) {
        this.chatRoom = chatRoom;
        this.message = message;
        this.sender = sender;
        this.isRead = false;
        this.sendAt = LocalDateTime.now();
        chatRoom.setLastMessageTime(this.sendAt);
    }

    public void setRead(Boolean isRead) {
        this.isRead = isRead;
    }
}
