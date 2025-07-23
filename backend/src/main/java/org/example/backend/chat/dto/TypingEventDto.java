package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프론트 → 서버로 타이핑 상태를 보낼 때 사용하는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingEventDto {
    private Long roomId;     // 타이핑 중인 채팅방
    private boolean typing;  // true: 타이핑 중, false: 멈춤
}