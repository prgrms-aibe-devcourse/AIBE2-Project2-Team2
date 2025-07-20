package org.example.backend.chat.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.chat.dto.ChatMessageRespondDto;
import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatMessageRepository;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "ChatMessage", description = "채팅 메시지 API")
public class ChatMessageApiController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    /**
     *  채팅 메시지 전송
     */
    @Operation(
            summary = "채팅 메시지 전송",
            description = "특정 채팅방(roomId)에 메시지를 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 전송 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatMessageRespondDto.class),
                            examples = @ExampleObject(
                                    value = "{ \"messageId\": 1, \"roomId\": 3, \"senderId\": 5, \"message\": \"안녕하세요!\", \"isRead\": false, \"sendAt\": \"2025-07-17T15:30:00\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 방의 멤버가 아님",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/rooms/{roomId}/send")
    public ResponseEntity<ChatMessageRespondDto> sendMessage(
            @PathVariable Long roomId,
            @RequestParam String message
    ) {
        String myEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        Member sender = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        //  사용자가 이 방의 멤버인지 검증
        validateRoomMember(room, sender);

        ChatMessage savedMessage = chatMessageRepository.save(
                ChatMessage.builder()
                        .chatRoom(room)
                        .senderId(
                                Optional.ofNullable(sender.getMemberId())
                                        .orElseThrow(() -> new IllegalStateException("SenderId is null!"))
                        )
                        .message(message)
                        .build()
        );

        return ResponseEntity.ok(ChatMessageRespondDto.from(savedMessage, sender.getNickname()));
    }

    /**
     *  채팅 메시지 조회
     */
    @Operation(
            summary = "채팅 메시지 조회",
            description = "특정 채팅방(roomId)의 모든 메시지를 시간순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "메시지 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatMessageRespondDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 방의 멤버가 아님",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "채팅방을 찾을 수 없음",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessageRespondDto>> getMessages(
            @Parameter(description = "채팅방 ID", example = "1") @PathVariable Long roomId
    ) {
        String myEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        Member requester = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 사용자가 이 방의 멤버인지 검증
        validateRoomMember(room, requester);

        List<ChatMessageRespondDto> messages = chatMessageRepository.findByChatRoomOrderBySendAtAsc(room)
                .stream()
                .map(msg -> {
                    Long senderId = msg.getSenderId();
                    if (senderId == null) {
                        // senderId가 없으면 기본 사용자명으로 반환
                        return ChatMessageRespondDto.from(msg, "알 수 없는 사용자");
                    }
                    return memberRepository.findById(senderId)
                            .map(sender -> ChatMessageRespondDto.from(msg, sender.getNickname()))
                            .orElseGet(() -> ChatMessageRespondDto.from(msg, "알 수 없는 사용자"));
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    /**
     *  방 멤버 검증 메서드 (공통)
     */
    private void validateRoomMember(ChatRoom room, Member member) {
        if (!room.getMember1().equals(member) && !room.getMember2().equals(member)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 채팅방에 속하지 않은 사용자입니다.");
        }
    }
}