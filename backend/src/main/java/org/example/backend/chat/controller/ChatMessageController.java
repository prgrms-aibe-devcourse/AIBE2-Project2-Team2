package org.example.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.chat.dto.ChatMessageRespondDto;
import org.example.backend.chat.service.ChatMessageService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms/{roomId}")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    /**
     * 특정 채팅방 메시지 조회
     */
    @GetMapping("/messages")
    public List<ChatMessageRespondDto> getMessages(
            @PathVariable Long roomId,
            Principal principal   // ✅ JWT에서 인증된 사용자
    ) {
        String myEmail = principal.getName(); // JWTFilter에서 넣은 username(email)
        return chatMessageService.getMessages(roomId);
    }
}