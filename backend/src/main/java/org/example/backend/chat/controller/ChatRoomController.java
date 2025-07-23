package org.example.backend.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatRoomDto;
import org.example.backend.chat.service.ChatRoomService;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatMessageRepository;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
@Tag(name = "ChatRoom", description = "채팅방 관련 API")  // ✅ Swagger 그룹 이름
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;

    /**
     * ✅ 특정 사용자와 1:1 채팅방 생성 (이미 존재하면 반환)
     */
    @Operation(
            summary = "채팅방 생성 or 기존 방 반환",
            description = "특정 사용자와 1:1 채팅방을 생성합니다. 이미 존재하면 기존 방 정보를 반환합니다."
    )
    @PostMapping("/create")
    public ResponseEntity<ChatRoomDto> createOrFindRoom(@RequestParam Long targetId) {

        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }

        ChatRoomDto roomDto = chatRoomService.findOrCreateChatRoomByEmail(myEmail, targetId);
        return ResponseEntity.ok(roomDto);
    }

    /**
     * ✅ 내가 속한 모든 채팅방 목록 조회
     */
    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "로그인한 사용자가 참여 중인 모든 채팅방 목록을 조회합니다."
    )
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getMyRooms() {

        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }

        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보 없음"));

        // ✅ 내가 속한 모든 채팅방 조회 (findAllByMemberId 사용)
        List<ChatRoom> rooms = chatRoomRepository.findAllByMemberId(me.getMemberId());

        List<ChatRoomDto> response = rooms.stream()
                .map(ChatRoomDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * ✅ 특정 채팅방 상세 정보 조회
     */
    @Operation(
            summary = "채팅방 상세 조회",
            description = "특정 채팅방의 상세 정보를 조회합니다. (참여자만 조회 가능)"
    )
    @GetMapping("/{roomId}")
    public ResponseEntity<ChatRoomDto> getRoomDetail(@PathVariable Long roomId) {

        String myEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 필요");
        }

        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보 없음"));

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "채팅방 없음"));

        // ✅ 방 참여자 검증
        if (!room.getMember1().equals(me) && !room.getMember2().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "이 방에 접근 권한이 없습니다.");
        }

        return ResponseEntity.ok(ChatRoomDto.from(room));
    }
}