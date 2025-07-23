package org.example.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatMessageRespondDto;
import org.example.backend.chat.dto.ChatRoomDto;
import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    /**
     * ✅ 기존 채팅방이 있으면 조회, 없으면 생성
     */
    @Transactional
    public ChatRoomDto findOrCreateChatRoomByEmail(String myEmail, Long targetId) {
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다.\""));
        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("상대 정보를 찾을 수 없습니다."));

        ChatRoom chatRoom = chatRoomRepository.findRoomBetween(
                me.getMemberId(),
                target.getMemberId()
        ).orElseGet(() -> {
            ChatRoom newRoom = ChatRoom.builder()
                    .member1(me)
                    .member2(target)
                    .build();
            return chatRoomRepository.save(newRoom);
        });

        return ChatRoomDto.from(chatRoom);
    }

    /**
     * ✅ 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(Long roomId, String senderEmail, String messageContent) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다.\""));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(messageContent)
                .build();

        return chatMessageRepository.save(message);
    }

    /**
     * ✅ 특정 채팅방 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageRespondDto> getMessages(Long chatRoomId) {
        chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        return chatMessageRepository.findAllByChatRoom_ChatroomIdOrderBySendAtAsc(chatRoomId)
                .stream()
                .map(ChatMessageRespondDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 내가 속한 모든 채팅방 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getMyChatRooms(String myEmail) {
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다.\""));

        // member1 또는 member2가 나인 모든 방 조회
        List<ChatRoom> myRooms = chatRoomRepository.findAllByMember(me.getMemberId());

        return myRooms.stream()
                .map(ChatRoomDto::from)
                .collect(Collectors.toList());
    }

    /**
     * ✅ 특정 채팅방 상세 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDto getChatRoomDetail(Long chatRoomId, String myEmail) {
        // 사용자 검증
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));

        // 채팅방 검증
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // ✅ 본인이 속한 방인지 확인
        if (!chatRoom.getMember1().getMemberId().equals(me.getMemberId()) &&
                !chatRoom.getMember2().getMemberId().equals(me.getMemberId())) {
            throw new IllegalArgumentException("본인이 속한 채팅방이 아님");
        }

        return ChatRoomDto.from(chatRoom);
    }

    /**
     * ✅ 채팅방 메시지 읽음 처리
     * - 내가 보낸 메세지를 전부 읽음 처리
     */
    @Transactional
    public void markAsRead(Long chatRoomId, String myEmail) {
        // 사용자 검증
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자 없음"));

        // 채팅방 검증
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        Long myId = me.getMemberId();

        chatMessageRepository.markMessagesAsRead(chatRoomId, myId);
    }

    /**
     * ✅ 특정 채팅방의 모든 멤버 이메일 조회
     */
    @Transactional(readOnly = true)
    public List<String> getRoomMembers(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // 방에 있는 두 명의 이메일 반환
        return List.of(
                chatRoom.getMember1().getEmail(),
                chatRoom.getMember2().getEmail()
        );
    }
}