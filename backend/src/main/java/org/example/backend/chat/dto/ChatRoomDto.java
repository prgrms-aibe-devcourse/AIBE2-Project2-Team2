package org.example.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.backend.entity.ChatRoom;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomDto {

    @Schema(description = "채팅방 ID", example = "3")
    private Long roomId;

    @Schema(description = "내 이메일", example = "me@test.com")
    private String myEmail;

    @Schema(description = "상대방 이메일", example = "opponent@test.com")
    private String opponentEmail;

    @Schema(description = "상대방 닉네임", example = "홍길동")
    private String opponentName;

    @Schema(description = "상대방 프로필 이미지", example = "https://cdn.example.com/profile/hong.jpg")
    private String opponentProfileImage;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    public static ChatRoomDto from(ChatRoom room,
                                   String myEmail,
                                   String opponentProfileImage) {

        boolean amIUser1 = room.getMember1().getEmail().equals(myEmail);

        String opponentEmail = amIUser1 ? room.getMember2().getEmail() : room.getMember1().getEmail();
        String opponentName  = amIUser1 ? room.getMember2().getNickname() : room.getMember1().getNickname();

        return ChatRoomDto.builder()
                .roomId(room.getChatroomId())
                .myEmail(myEmail)
                .opponentEmail(opponentEmail)
                .opponentName(opponentName)
                .opponentProfileImage(opponentProfileImage)
                .lastMessage(null)   // 필요하면 최근 메시지 넣기
                .lastMessageTime(null)
                .build();
    }
}