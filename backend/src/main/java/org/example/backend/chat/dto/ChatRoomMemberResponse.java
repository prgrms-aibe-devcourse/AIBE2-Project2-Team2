package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.backend.entity.Member;

@Getter
@AllArgsConstructor
public class ChatRoomMemberResponse {
    private Long memberId;
    private String username;

    public static ChatRoomMemberResponse from(Member member) {
        return new ChatRoomMemberResponse(
                member.getMemberId(),
                member.getNickname()  // username 대신 nickname 사용
        );
    }
}