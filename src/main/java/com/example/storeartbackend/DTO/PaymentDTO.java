package com.example.storeartbackend.DTO;

import lombok.Data;

@Data
public class PaymentDTO {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method;
    private Long totalAmount; // Long으로 선언
    private Long balanceAmount; // Long으로 선언
    private String currency;
    private String status;
    private String approvedAt;
    private String receiptUrl;
    private int userIdx;
}