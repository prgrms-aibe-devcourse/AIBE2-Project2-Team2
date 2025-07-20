package org.example.backend.repository;

import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 특정 두 멤버의 방이 있는지 확인
    Optional<ChatRoom> findByMember1AndMember2(Member member1, Member member2);

    // 특정 멤버가 참여한 방 전부 조회
    List<ChatRoom> findByMember1OrMember2(Member member1, Member member2);

    // 특정 두 멤버 간의 채팅방을 양방향으로 검색
    Optional<ChatRoom> findByMember1AndMember2OrMember1AndMember2(
            Member member1, Member member2,
            Member member3, Member member4
    );
}
