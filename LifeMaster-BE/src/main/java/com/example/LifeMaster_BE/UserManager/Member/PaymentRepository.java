package com.example.LifeMaster_BE.UserManager.Member;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
}