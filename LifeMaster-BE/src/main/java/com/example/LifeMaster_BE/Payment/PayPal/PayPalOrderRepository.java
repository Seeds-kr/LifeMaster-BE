package com.example.LifeMaster_BE.Payment.PayPal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayPalOrderRepository extends JpaRepository<PayPalOrderEntity, Long> {
    Optional<PayPalOrderEntity> findByPaypalOrderId(String paypalOrderId);
}