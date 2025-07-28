package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.entity.ChatMessage;

import java.time.LocalDateTime;

// ✅ 서버가 응답할 때
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRespondDto {
    private Long chatId;          // 메시지 PK
    private Long roomId;          // 채팅방 ID
    private String senderEmail;   // 보낸 사람 이메일
    private String senderName;    // 보낸 사람 닉네임
    private String message;       // 메시지 내용
    private LocalDateTime sendAt; // 보낸 시각

    public static ChatMessageRespondDto from(ChatMessage chatMessage) {
        return ChatMessageRespondDto.builder()
                .chatId(chatMessage.getChatId())
                .roomId(chatMessage.getChatRoom().getChatroomId())
                .senderEmail(chatMessage.getSender().getEmail())
                .senderName(chatMessage.getSender().getNickname())
                .message(chatMessage.getMessage())
                .sendAt(chatMessage.getSendAt())
                .build();
    }
}