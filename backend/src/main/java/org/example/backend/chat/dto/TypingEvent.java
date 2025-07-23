package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket에서 주고받는 "누가 타이핑 중인지"를 알리는 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingEvent {
    private Long roomId;     // 어떤 채팅방인지
    private Long senderId;   // 누가 타이핑 중인지
    private String senderName; // 닉네임
    private boolean typing;  // true: 타이핑 중, false: 멈춤
}