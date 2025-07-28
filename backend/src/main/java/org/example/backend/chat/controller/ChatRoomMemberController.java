package org.example.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.chat.dto.ChatRoomMemberResponse;
import org.example.backend.chat.service.ChatRoomMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms/{chatRoomId}/members")
@RequiredArgsConstructor
public class ChatRoomMemberController {

    private final ChatRoomMemberService chatRoomMemberService;

    /**
     * 채팅방 참여자 정보 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomMemberResponse>> getChatRoomMembers(
            @PathVariable Long chatRoomId
    ) {
        List<ChatRoomMemberResponse> members = chatRoomMemberService.getChatRoomMembers(chatRoomId);
        return ResponseEntity.ok(members);
    }
}