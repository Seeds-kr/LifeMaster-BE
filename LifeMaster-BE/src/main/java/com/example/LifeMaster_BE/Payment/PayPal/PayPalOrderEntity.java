package com.example.LifeMaster_BE.Payment.PayPal;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "paypal_order")
@Getter
@Setter
public class PayPalOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paypal_order_id", nullable = false, unique = true, length = 100)
    private String paypalOrderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @Column(nullable = false, length = 30)
    private String status; // CREATED, COMPLETED, CANCELLED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}