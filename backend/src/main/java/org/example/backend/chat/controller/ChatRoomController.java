package org.example.backend.chat.controller;

import lombok.RequiredArgsConstructor;
import org.example.backend.chat.dto.ChatRoomDto;
import org.example.backend.chat.dto.ChatRoomListDto;
import org.example.backend.chat.dto.TargetEmailRequest;
import org.example.backend.chat.service.ChatRoomService;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatRoomMemberRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * 채팅방 생성 또는 기존 방 찾기
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<ChatRoomDto> findOrCreateChatRoom(
            Principal principal,            // ✅ 현재 로그인된 사용자
            @RequestBody TargetEmailRequest req
    ) {
        String myEmail = principal.getName();  // JWTFilter가 넣어준 email
        ChatRoom room = chatRoomService.findOrCreateChatRoomByEmail(myEmail, req.getTargetEmail());

        // TODO : 이후 소켓화 하기 위한 분리
        // 프로필 이미지, 안읽은 메세지 수 가져오기
        Member opponent = memberRepository.findByEmail(req.getTargetEmail()).orElseThrow(RuntimeException::new);
        String opponentProfileImg = opponent.getProfileImageUrl();

        return ResponseEntity.ok(
                ChatRoomDto.from(room, myEmail, opponentProfileImg)
        );
    }

    /**
     * 내가 속한 모든 채팅방 조회
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomListDto>> getMyChatRooms(Principal principal) {
        String myEmail = principal.getName();
        List<ChatRoomListDto> myRooms = chatRoomService.getMyChatRooms(myEmail);
        return ResponseEntity.ok(myRooms);
    }
}