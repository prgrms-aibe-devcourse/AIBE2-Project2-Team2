package org.example.backend.payment.dto;

import lombok.Data;

@Data
public class KakaoPayApproveRequest {
    private String cid;
    private String tid;
    private String partner_order_id;
    private String partner_user_id;
    private String pg_token;
    // 필요시 추가 필드
} 