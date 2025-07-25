package org.example.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// ✅ 클라이언트가 메시지를 전송할 때
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequestDto {
    @NotNull
    private Long roomId;        // 채팅방 ID
    @NotBlank
    private String senderEmail; // 보낸 사람 이메일
    @NotBlank
    private String message;     // 메시지 내용
}