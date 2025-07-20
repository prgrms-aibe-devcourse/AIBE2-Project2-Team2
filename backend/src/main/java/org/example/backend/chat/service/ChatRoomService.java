package org.example.backend.chat.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    /**
     *  기존 채팅방이 있으면 조회, 없으면 생성
     */
    public ChatRoomDto findOrCreateChatRoomByEmail(String myEmail, Long targetId) {
        Member me = memberRepository.findByEmail(myEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자 없음"));
        Member target = memberRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("상대 정보 없음"));

        ChatRoom chatRoom = chatRoomRepository.findByMember1AndMember2OrMember1AndMember2(
                me, target, target, me
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
     *  메시지 저장
     */
    @Transactional
    public ChatMessage saveMessage(Long chatRoomId, Long senderId, String message) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .senderId(senderId)
                .message(message)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        //  저장 로그 추가
        System.out.println("[채팅 저장 완료] chatRoomId=" + chatRoomId +
                ", senderId=" + senderId +
                ", message=" + message);

        return chatMessageRepository.save(chatMessage);
    }

    /**
     *  특정 채팅방 메시지 조회
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 없음"));
        return chatMessageRepository.findByChatRoomOrderBySendAtAsc(chatRoom);
    }
}