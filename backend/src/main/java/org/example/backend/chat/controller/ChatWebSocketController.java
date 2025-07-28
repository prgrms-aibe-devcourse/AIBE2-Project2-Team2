package org.example.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.*;
import org.example.backend.chat.service.ChatMessageService;
import org.example.backend.chat.service.ChatRoomMemberService;
import org.example.backend.chat.service.ChatRoomService;
import org.example.backend.entity.ChatMessage;
import org.example.backend.repository.MemberRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ✅ 메시지 발송 (WebSocket)
     * - 사용자가 `/pub/chat/{roomId}.send` 로 메시지를 보내면,
     * - DB에 저장 후 `/sub/chatroom/{roomId}` 를 구독한 사용자들에게 브로드캐스트
     */
    @MessageMapping("/chat/{roomId}.send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequestDto request,
            Principal principal
    ) {
        String senderEmail = principal.getName(); // ✅ 현재 로그인 유저 이메일
        log.info("📩 메시지 수신 roomId={} sender={} msg={}", roomId, senderEmail, request.getMessage());

        // ✅ 메시지 DB 저장
        ChatMessage savedMessage = chatMessageService.saveMessage(
                roomId,
                senderEmail,
                request.getMessage()
        );

        // ✅ 응답 DTO 변환
        ChatMessageRespondDto response = ChatMessageRespondDto.from(savedMessage);

        // ✅ 채팅방 구독자들에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId, response);
    }
}