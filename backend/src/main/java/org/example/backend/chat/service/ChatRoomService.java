package org.example.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatRoomListDto;
import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.ChatRoomMember;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatMessageRepository;
import org.example.backend.repository.ChatRoomMemberRepository;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * ✅ 기존 채팅방이 있으면 조회, 없으면 생성
     */
    @Transactional
    public ChatRoom findOrCreateChatRoomByEmail(String myEmail, String targetEmail) {
        Member me = findMemberByEmail(myEmail);
        Member target = findMemberByEmail(targetEmail);

        return chatRoomRepository.findRoomBetween(
                me.getMemberId(),
                target.getMemberId()
        ).orElseGet(() -> {
            // 1️⃣ ChatRoom 먼저 생성 후 저장
            ChatRoom newRoom = ChatRoom.builder()
                    .member1(me)
                    .member2(target)
                    .build();
            ChatRoom savedRoom = chatRoomRepository.save(newRoom);

            // 2️⃣ 저장된 ChatRoom을 참조해서 ChatRoomMember 생성
            ChatRoomMember myMember = new ChatRoomMember(savedRoom, me);
            ChatRoomMember targetMember = new ChatRoomMember(savedRoom, target);

            // 3️⃣ ChatRoomMember 저장
            chatRoomMemberRepository.save(myMember);
            chatRoomMemberRepository.save(targetMember);

            return savedRoom;
        });
    }

    /**
     * ✅ 내가 속한 모든 채팅방 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getMyChatRooms(String myEmail) {
        Member me = findMemberByEmail(myEmail);
        List<ChatRoom> myRooms = chatRoomRepository.findAllByMemberIdOrderByLastMessageDesc(me.getMemberId());

        return myRooms.stream().map(room -> {
            // 상대방 이름 구하기
            String opponentName = room.getMember1().getEmail().equals(myEmail)
                    ? room.getMember2().getNickname()
                    : room.getMember1().getNickname();

            // 가장 최근 메시지 가져오기
            ChatMessage lastMessage = chatMessageRepository.findTop1ByChatRoom_ChatroomIdOrderBySendAtDesc(room.getChatroomId())
                    .orElse(null);
            String lastMessageText = lastMessage != null ? lastMessage.getMessage() : null;

            // 읽지 않은 메시지 개수
            ChatRoomMember crmMe =  chatRoomMemberRepository.findByChatRoomAndMember(room, me)
                    .orElse(null);

            int hasUnread = (crmMe != null) ? crmMe.getUnreadCount() : 0;

            return new ChatRoomListDto(
                    room.getChatroomId(),
                    opponentName,
                    lastMessageText
            );
        }).collect(Collectors.toList());
    }

    /**
     * ✅ 특정 채팅방의 모든 멤버 이메일 조회
     */
    @Transactional(readOnly = true)
    public List<String> getRoomMembers(Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        return List.of(
                chatRoom.getMember1().getEmail(),
                chatRoom.getMember2().getEmail()
        );
    }

    // ✅ 공통 로직 (중복 제거)
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("상대 정보를 찾을 수 없습니다."));
    }

    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    private boolean isMemberInRoom(ChatRoom chatRoom, Long memberId) {
        return chatRoom.getMember1().getMemberId().equals(memberId) ||
                chatRoom.getMember2().getMemberId().equals(memberId);
    }
}