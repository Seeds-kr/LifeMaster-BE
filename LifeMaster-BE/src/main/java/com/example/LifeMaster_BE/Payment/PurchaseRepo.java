package com.example.LifeMaster_BE.Payment;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepo extends JpaRepository<PurchaseEntity, String> {

    // 단건 조회
    Optional<PurchaseEntity> findByOrderId(String orderId);

    // 회원별 최신순 전체
    List<PurchaseEntity> findAllByMember_IdOrderByPurchaseTimeDesc(Long memberId);

    // 회원 + 결제원(provider) 필터 + 최신순
    List<PurchaseEntity> findAllByMember_IdAndProviderOrderByPurchaseTimeDesc(Long memberId, String provider);

    // 페이지네이션 (정렬은 Pageable에서 지정)
    Page<PurchaseEntity> findAllByMember_Id(Long memberId, Pageable pageable);

    // 기간 필터 + 최신순
    List<PurchaseEntity> findAllByMember_IdAndPurchaseTimeBetweenOrderByPurchaseTimeDesc(
            Long memberId, LocalDateTime start, LocalDateTime end
    );

    // 존재 여부(멱등성 등)
    boolean existsByPurchaseToken(String purchaseToken);
    boolean existsByOrderId(String orderId);

    // 회원 + 주문ID
    Optional<PurchaseEntity> findByMember_IdAndOrderId(Long memberId, String orderId);

    // 어드민 전체 조회용
    Page<PurchaseEntity> findAllByOrderByPurchaseTimeDesc(Pageable pageable);

    // 필요하면 회원별 조회도 pageable 버전 추가 가능
    Page<PurchaseEntity> findAllByMember(MemberEntity member, Pageable pageable);
}
