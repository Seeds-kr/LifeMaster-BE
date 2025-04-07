package com.example.LifeMaster_BE.Payment;


import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepo extends JpaRepository<PurchaseEntity, String> {
    Optional<PurchaseEntity> findByOrderId(String orderId);

    List<PurchaseEntity> findByMember(MemberEntity member);
}