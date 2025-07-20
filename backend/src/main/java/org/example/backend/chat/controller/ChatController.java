package org.example.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatMessageRequestDto;
import org.example.backend.chat.dto.ChatMessageRespondDto;
import org.example.backend.chat.service.ChatRoomService;
import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.Member;
import org.example.backend.repository.MemberRepository;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;

    /**
     *  클라이언트가 /pub/chat.send 로 메시지 전송하면 실행됨
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload ChatMessageRequestDto requestDto, Principal principal) {
        try {
            log.info("📩 Received ChatMessageRequestDto: {}", requestDto);

            //  인증된 사용자 이메일 가져오기
            String email = principal.getName();
            Member sender = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

            //  메시지 DB 저장 (senderId는 인증된 사용자 기준)
            ChatMessage saved = chatRoomService.saveMessage(
                    requestDto.getRoomId(),
                    sender.getMemberId(),
                    requestDto.getMessage()
            );

            //  응답 DTO 변환 (추가로 senderName까지 내려주면 프론트에서 바로 출력 가능)
            ChatMessageRespondDto response = ChatMessageRespondDto.from(saved, sender.getNickname());

            //  /sub/chatroom/{roomId} 구독 중인 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend(
                    "/sub/chatroom/" + requestDto.getRoomId(),
                    response
            );

            log.info("✅ Sent to /sub/chatroom/{} : {}", requestDto.getRoomId(), response);

        } catch (IllegalArgumentException e) {
            // 채팅방 없음 등 잘못된 요청 처리
            log.warn("❌ Chat sendMessage 실패: {}", e.getMessage());
            messagingTemplate.convertAndSend(
                    "/sub/chatroom/errors",
                    "메시지 전송 실패: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("❌ Chat sendMessage 내부 에러", e);
            messagingTemplate.convertAndSend(
                    "/sub/chatroom/errors",
                    "서버 오류로 메시지 전송 실패"
            );
        }
    }

    /**
     *  WebSocket 메시지 예외 처리 (모든 MessageMapping 에서 공통 적용)
     */
    @MessageExceptionHandler
    public void handleWebSocketException(Exception e) {
        log.error("❌ WebSocket 전송 중 예외 발생", e);
        // 에러 발생 시 클라이언트가 /sub/chatroom/errors 구독하면 받을 수 있음
        messagingTemplate.convertAndSend("/sub/chatroom/errors", "에러: " + e.getMessage());
    }
}