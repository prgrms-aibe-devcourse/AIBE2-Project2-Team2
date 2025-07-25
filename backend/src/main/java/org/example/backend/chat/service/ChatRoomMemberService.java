package org.example.backend.chat.service;

import lombok.RequiredArgsConstructor;
import org.example.backend.chat.dto.ChatRoomMemberResponse;
import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.ChatRoomMember;
import org.example.backend.entity.Member;
import org.example.backend.repository.ChatMessageRepository;
import org.example.backend.repository.ChatRoomMemberRepository;
import org.example.backend.repository.ChatRoomRepository;
import org.example.backend.repository.MemberRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomMemberService {

    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * ✅ 이메일로 사용자 찾기 (공통 로직)
     */
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인된 사용자를 찾을 수 없습니다."));
    }

    /**
     * ✅ ID로 사용자 찾기
     */
    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    /**
     * ✅ 채팅방 존재 여부 검증 후 반환
     */
    @Transactional(readOnly = true)
    public ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }

    /**
     * ✅ 특정 사용자가 채팅방에 속해있는지 검증
     */
    @Transactional(readOnly = true)
    public boolean isMemberInChatRoom(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        return chatRoom.getMember1().getMemberId().equals(memberId) ||
                chatRoom.getMember2().getMemberId().equals(memberId);
    }

    /**
     * ✅ 채팅방 참여자인지 확인하고, 아니라면 예외 발생
     */
    @Transactional(readOnly = true)
    public void validateMemberInChatRoom(Long chatRoomId, Long memberId) {
        if (!isMemberInChatRoom(chatRoomId, memberId)) {
            throw new IllegalArgumentException("본인이 속한 채팅방이 아닙니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ChatRoomMemberResponse> getChatRoomMembers(Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        List<ChatRoomMemberResponse> members = new ArrayList<>();

        // member1
        members.add(ChatRoomMemberResponse.from(chatRoom.getMember1()));
        // member2
        members.add(ChatRoomMemberResponse.from(chatRoom.getMember2()));

        return members;
    }
}