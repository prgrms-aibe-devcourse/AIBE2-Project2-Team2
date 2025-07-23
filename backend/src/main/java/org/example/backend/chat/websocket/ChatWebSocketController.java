package org.example.backend.chat.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.TypingEventDto;
import org.example.backend.chat.dto.TypingEvent;
import org.example.backend.entity.Member;
import org.example.backend.repository.MemberRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;

    @MessageMapping("/chat/typing")
    public void typing(TypingEventDto dto, Principal principal) {
        // JWT에서 sender 정보 추출
        String senderEmail = principal.getName();
        Member member = memberRepository.findByEmail(senderEmail).orElseThrow();

        TypingEvent event = new TypingEvent(
                dto.getRoomId(),
                member.getMemberId(),
                member.getNickname(),
                dto.isTyping()
        );

        // 같은 방의 다른 사람들에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/sub/chatroom/" + dto.getRoomId() + "/typing",
                event
        );
    }
}
