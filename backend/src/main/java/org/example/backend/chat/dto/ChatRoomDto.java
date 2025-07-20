package org.example.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.entity.ChatRoom;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    @Schema(description = "채팅방 ID", example = "3")
    private Long roomId;

    @Schema(description = "첫 번째 사용자 ID", example = "1")
    private Long member1Id;

    @Schema(description = "첫 번째 사용자 닉네임", example = "나")
    private String member1Name;

    @Schema(description = "두 번째 사용자 ID", example = "2")
    private Long member2Id;

    @Schema(description = "두 번째 사용자 닉네임", example = "홍길동")
    private String member2Name;

    public static ChatRoomDto from(ChatRoom room) {
        return ChatRoomDto.builder()
                .roomId(room.getChatroomId())
                .member1Id(room.getMember1().getMemberId())
                .member1Name(room.getMember1().getNickname())
                .member2Id(room.getMember2().getMemberId())
                .member2Name(room.getMember2().getNickname())
                .build();
    }
}