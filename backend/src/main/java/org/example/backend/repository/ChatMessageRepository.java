package org.example.backend.repository;

import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 특정 채팅방 메시지 전부 가져오기
    List<ChatMessage> findByChatRoomOrderBySendAtAsc(ChatRoom chatRoom);
}
