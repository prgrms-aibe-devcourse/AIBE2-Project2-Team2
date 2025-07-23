package org.example.backend.repository;

import org.example.backend.entity.ChatMessage;
import org.example.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * ✅ 특정 채팅방의 모든 메시지를 오래된 순으로 조회
     */
    List<ChatMessage> findAllByChatRoom_ChatroomIdOrderBySendAtAsc(Long chatRoomId);

    /**
     * ✅ 특정 채팅방의 마지막(최근) 메시지 가져오기 (채팅방 목록 미리보기용)
     */
    ChatMessage findTop1ByChatRoom_ChatroomIdOrderBySendAtDesc(Long chatRoomId);

    /**
     * ✅ 읽지 않은 메시지를 읽음 처리
     */
    @Modifying
    @Query("UPDATE ChatMessage m " +
            "SET m.isRead = true " +
            "WHERE m.chatRoom.chatroomId = :roomId " +
            "AND m.sender.memberId = :myId " +
            "AND m.isRead = false")
    int markMessagesAsRead(@Param("roomId") Long roomId,
                           @Param("myId") Long myId);

    /**
     * ✅ 특정 채팅방의 읽지 않은 메시지 개수 가져오기
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
            "WHERE m.chatRoom.chatroomId = :roomId " +
            "AND m.isRead = false")
    long countUnreadMessages(@Param("roomId") Long roomId);
}