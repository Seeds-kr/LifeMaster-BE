package com.example.LifeMaster_BE.Payment;


import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRepo extends JpaRepository<PurchaseEntity, String> {

    // 단건 조회
    Optional<PurchaseEntity> findByOrderId(String orderId);

    // ❌ 기존: 정렬/페이징 없음
    // List<PurchaseEntity> findByMember(MemberEntity member);

    // ✅ 회원별 최신순 전체 (프런트에서 빠르게 쓰기 좋음)
    List<PurchaseEntity> findAllByMember_IdOrderByPurchaseTimeDesc(Long memberId);

    // ✅ 회원 + 결제원(provider) 필터 + 최신순
    List<PurchaseEntity> findAllByMember_IdAndProviderOrderByPurchaseTimeDesc(Long memberId, String provider);

    // ✅ 페이지네이션(권장): 큰 데이터에도 안전
    Page<PurchaseEntity> findAllByMember_Id(Long memberId, Pageable pageable);

    // ✅ 기간 필터(대시보드/검색용)
    List<PurchaseEntity> findAllByMember_IdAndPurchaseTimeBetweenOrderByPurchaseTimeDesc(
            Long memberId, LocalDateTime start, LocalDateTime end
    );

    // ✅ 존재 여부(중복 방지 등)
    boolean existsByPurchaseToken(String purchaseToken);
    boolean existsByOrderId(String orderId);

    // ✅ 회원 + 주문ID(콜백 매핑시 유용)
    Optional<PurchaseEntity> findByMember_IdAndOrderId(Long memberId, String orderId);

    // (선택) 카운트/합계 같은 통계가 필요하면 @Query로 Projection 추가 가능
}