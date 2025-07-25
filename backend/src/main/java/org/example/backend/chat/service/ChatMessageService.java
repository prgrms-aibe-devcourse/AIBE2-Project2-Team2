package org.example.backend.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.chat.dto.ChatMessageRespondDto;
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
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    /**
     * ✅ 메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(Long roomId, String senderEmail, String messageContent) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        Member sender = memberRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사람 없음"));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(messageContent)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        chatRoom.setLastMessageTime(savedMessage.getSendAt());

        String opponentEmail = chatRoom.getMember1().getEmail().equals(senderEmail)
                ? chatRoom.getMember2().getEmail()
                : chatRoom.getMember1().getEmail();

        Member opponent = memberRepository.findByEmail(opponentEmail)
                .orElseThrow(() -> new IllegalArgumentException("상대방 없음"));

        // ChatRoomMember 가져오기
        ChatRoomMember opponentRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, opponent)
                .orElseThrow(() -> new IllegalStateException("상대방의 ChatRoomMember가 없음"));

        return savedMessage;
    }

    /**
     * ✅ 특정 채팅방 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessageRespondDto> getMessages(Long chatRoomId) {
        findChatRoomById(chatRoomId); // 채팅방 존재 여부 검증
        return chatMessageRepository.findAllByChatRoom_ChatroomIdOrderBySendAtAsc(chatRoomId)
                .stream()
                .map(ChatMessageRespondDto::from)
                .collect(Collectors.toList());
    }

    // ✅ 공통 로직
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
    }

    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }
}