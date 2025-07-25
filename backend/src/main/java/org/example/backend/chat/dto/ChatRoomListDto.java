package org.example.backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatRoomListDto {
    private Long roomId;
    private String opponentName;
    private String lastMessage;
}
