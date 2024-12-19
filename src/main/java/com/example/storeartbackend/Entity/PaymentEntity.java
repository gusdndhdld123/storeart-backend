package com.example.storeartbackend.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment")
@Builder
public class PaymentEntity extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentIdx;

    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method;
    private Long totalAmount;
    private Long balanceAmount;
    private String currency;
    private String status;
    private String approvedAt;
    private String receiptUrl;
    private int userIdx;
}