package org.example.backend.payment.dto;

import lombok.Data;

@Data
public class KakaoPayCancelRequest {
    private String cid;
    private String tid;
    private int cancel_amount;
    private int cancel_tax_free_amount;
    private int cancel_vat_amount;
    private String cancel_reason;
} 