package org.example.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageRequestDto {

    @Schema(description = "채팅방 ID", example = "3")
    private Long roomId;

    @Schema(description = "보낸 사람 ID", example = "5")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String message;
}
