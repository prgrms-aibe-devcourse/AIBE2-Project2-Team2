package org.example.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ChatMessageRespondDto {

    @Schema(description = "메시지 ID", example = "1")
    private Long messageId;

    @Schema(description = "채팅방 ID", example = "3")
    private Long roomId;

    @Schema(description = "보낸 사람 ID", example = "5")
    private Long senderId;

    private String senderName;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String message;

    @Schema(description = "읽음 여부", example = "false")
    private boolean read;

    @Schema(description = "전송 시각", example = "2025-07-17 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendAt;

    public static ChatMessageRespondDto from(ChatMessage msg, String senderName) {
        return ChatMessageRespondDto.builder()
                .messageId(msg.getChatId())
                .roomId(msg.getChatRoom().getChatroomId())
                .senderId(msg.getSenderId())
                .senderName(senderName)
                .message(msg.getMessage())
                .read(msg.getIsRead())
                .sendAt(msg.getSendAt())
                .build();
    }
}
