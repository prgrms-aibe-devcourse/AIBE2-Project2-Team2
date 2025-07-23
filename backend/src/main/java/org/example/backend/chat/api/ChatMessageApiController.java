package org.example.backend.chat.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatMessageRequestDto;
import org.example.backend.chat.dto.ChatMessageRespondDto;
import org.example.backend.chat.dto.MemberDto;
import org.example.backend.chat.service.ChatRoomService;
import org.example.backend.entity.ChatMessage;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "ChatMessage", description = "채팅 메시지 API")
public class ChatMessageApiController {

    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    private SimpUserRegistry simpUserRegistry;

    /**
     * ✅ 임시 로그인: 전체 채팅 멤버 조회
     */
    @GetMapping("/members")
    public ResponseEntity<List<MemberDto>> getChatMembers() {
        List<MemberDto> members = memberRepository.findAll()
                .stream()
                .map(MemberDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }

    /**
     * ✅ 채팅 메시지 전송
     */
    @MessageMapping("/chat/{roomId}.send")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRespondDto dto,
            Principal principal) {

        log.info("📩 STOMP MESSAGE 수신: roomId={}, payload={}, principal={}", roomId, dto, principal);

        String myEmail = principal.getName();

        // 1) 채팅방 멤버 이메일 가져오기
        List<String> roomMembers = chatRoomService.getRoomMembers(roomId);

        // 2) 상대 이메일만 추출
        String opponentEmail = roomMembers.stream()
                .filter(email -> !email.equals(myEmail))
                .findFirst()
                .orElse(null);

        boolean isOtherInRoom = simpUserRegistry.getUsers().stream()
                // ✅ 상대 이메일과 일치하는 사용자만 필터링
                .filter(user -> user.getName().equals(opponentEmail))
                .flatMap(user -> user.getSessions().stream())
                .flatMap(session -> session.getSubscriptions().stream())
                .anyMatch(sub -> sub.getDestination().equals("/sub/chatroom/" + roomId));
        
        // 1) DB 저장
        ChatMessage saved = chatRoomService.saveMessage(dto.getRoomId(), myEmail, dto.getMessage());
        ChatMessageRespondDto response = ChatMessageRespondDto.from(saved);

        // 2) 채팅방 구독자에게 메시지 broadcast
        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId, response);


        if (isOtherInRoom) {
            // 상대가 채팅방에 있다면 자동 읽음 처리
            chatRoomService.markAsRead(roomId, myEmail);

            // 읽음 이벤트 broadcast
            messagingTemplate.convertAndSend("/sub/chatroom/" + roomId + "/read", "상대가 읽음");
            log.info("✅ 상대가 방에 있으므로 자동 읽음 처리됨!");
        }
        else {
            log.info("📩 상대가 방에 없으므로 단순 전송!");
        }
    }

    /**
     * ✅ 채팅 메시지 조회
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageRespondDto>> getMessages(@PathVariable Long roomId) {
        List<ChatMessageRespondDto> messages = chatRoomService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 방의 안읽은 메시지를 모두 읽음 처리
     */
    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<String> markRoomMessagesAsRead(
            @PathVariable Long roomId,
            @RequestParam String myEmail
    ) {
        // ✅ targetEmail이 현재 방을 구독 중인지 확인
        boolean isTargetInRoom = simpUserRegistry.getUsers().stream()
                .filter(user -> user.getName().equals(myEmail))
                .flatMap(user -> user.getSessions().stream())
                .flatMap(session -> session.getSubscriptions().stream())
                .anyMatch(sub -> sub.getDestination().equals("/sub/chatroom/" + roomId));

        if (!isTargetInRoom) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body("상대방이 현재 방에 없음 → 읽음 처리 안 함");
        }
        else {
            chatRoomService.markAsRead(roomId, myEmail);
        }

        messagingTemplate.convertAndSend("/sub/chatroom/" + roomId + "/read", myEmail);

        return ResponseEntity.ok("메시지를 읽음 처리했습니다.");
    }
}