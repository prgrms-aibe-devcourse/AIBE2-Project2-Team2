package org.example.backend.chat.dto;

import java.util.List;

public class ReadReceiptDto {
    private List<Long> messageIds;
    private Long readerId; // 누가 읽었는지

    public ReadReceiptDto(List<Long> messageIds, Long readerId) {
        this.messageIds = messageIds;
        this.readerId = readerId;
    }
}