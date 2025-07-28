package org.example.backend.repository;

import org.example.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * ✅ 내가 속한 모든 채팅방 조회
     */
    @Query("SELECT c FROM ChatRoom c WHERE c.member1.memberId = :memberId OR c.member2.memberId = :memberId")
    List<ChatRoom> findAllByMemberId(@Param("memberId") Long memberId);

    /**
     * ✅ 특정 상대와의 채팅방 찾기
     */
    @Query("SELECT c FROM ChatRoom c WHERE (c.member1.memberId = :m1 AND c.member2.memberId = :m2) OR (c.member1.memberId = :m2 AND c.member2.memberId = :m1)")
    Optional<ChatRoom> findRoomBetween(@Param("m1") Long member1Id, @Param("m2") Long member2Id);

    /**
     * ✅ 내가 속한 채팅방 + 최근 메시지 기준으로 정렬
     */
    @Query("SELECT c FROM ChatRoom c " +
            "WHERE c.member1.memberId = :memberId OR c.member2.memberId = :memberId " +
            "ORDER BY c.lastMessageTime DESC")
    List<ChatRoom> findAllByMemberIdOrderByLastMessageDesc(@Param("memberId") Long memberId);

    @Query("SELECT c FROM ChatRoom c " +
            "WHERE c.member1.memberId = :memberId OR c.member2.memberId = :memberId")
    List<ChatRoom> findAllByMember(@Param("memberId") Long memberId);
}