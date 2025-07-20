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
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatRoomDto;
import org.example.backend.chat.dto.MemberDto;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Tag(name = "ChatRoom", description = "채팅방 관련 API")
public class ChatRoomApiController {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Operation(
            summary = "채팅방 생성",
            description = "요청자(myId)와 상대방(targetId)로 채팅방을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 생성 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 채팅방",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomDto.class),
                            examples = @ExampleObject(value = "{\"roomId\": 3, \"member1Id\": 1, \"member1Name\": \"나\", \"member2Id\": 2, \"member2Name\": \"홍길동\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"사용자를 찾을 수 없습니다.\"")
                    )
            )
    })
    @PostMapping("/create")
    public ResponseEntity<ChatRoomDto> createRoom(
            @AuthenticationPrincipal UserDetails user,
            @Parameter(description = "상대방 ID", example = "2") @RequestParam Long targetId
    ) {
        String myEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (myEmail == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증된 사용자가 없습니다.");
        }

        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "내 계정을 찾을 수 없습니다."));
        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상대방 계정을 찾을 수 없습니다."));

        //  이미 존재하는지 양방향 모두 확인
        Optional<ChatRoom> existingRoom = chatRoomRepository.findByMember1AndMember2(me, target)
                .or(() -> chatRoomRepository.findByMember1AndMember2(target, me));

        if (existingRoom.isPresent()) {
            log.info("이미 존재하는 채팅방 반환 roomId={}", existingRoom.get().getChatroomId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ChatRoomDto.from(existingRoom.get()));
        }

        //  새로운 방 생성
        ChatRoom newRoom = chatRoomRepository.save(ChatRoom.builder()
                .member1(me)
                .member2(target)
                .build());

        return ResponseEntity.ok(ChatRoomDto.from(newRoom));
    }

    @Operation(
            summary = "내 채팅방 목록 조회",
            description = "내가 속한 모든 채팅방을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "채팅방 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChatRoomDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"사용자를 찾을 수 없습니다.\"")
                    )
            )
    })

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDto>> getMyRooms(@AuthenticationPrincipal UserDetails user) {
        String myEmail = user.getUsername(); // ✅ JWT에서 email이 담겼다고 가정
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        //  내가 포함된 모든 채팅방 조회
        List<ChatRoomDto> rooms = chatRoomRepository.findByMember1OrMember2(me, me)
                .stream()
                .map(ChatRoomDto::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rooms);
    }

    @Operation(
            summary = "유저 목록 조회",
            description = "채팅 가능한 모든 유저 목록을 조회합니다. (로그인 유저는 제외)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유저 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberDto.class),
                            examples = @ExampleObject(
                                    value = "[{\"memberId\":1,\"nickname\":\"홍길동\"},{\"memberId\":2,\"nickname\":\"김철수\"}]"
                            )
                    )
            )
    })
    @GetMapping("/members")
    public ResponseEntity<List<MemberDto>> getAllMembersForChat() {
        List<MemberDto> members = memberRepository.findAll()
                .stream()
                .map(MemberDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }
}