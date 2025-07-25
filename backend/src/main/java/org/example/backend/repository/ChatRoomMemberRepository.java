package org.example.backend.repository;

import org.example.backend.entity.ChatRoom;
import org.example.backend.entity.ChatRoomMember;
import org.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);

    boolean existsByChatRoomAndMember(ChatRoom chatRoom, Member member);

    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);
}