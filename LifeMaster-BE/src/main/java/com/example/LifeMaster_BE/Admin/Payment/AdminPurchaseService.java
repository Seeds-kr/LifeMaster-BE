package com.example.LifeMaster_BE.Admin.Payment;

import com.example.LifeMaster_BE.Payment.PurchaseEntity;
import com.example.LifeMaster_BE.Payment.PurchaseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPurchaseService {

    private final PurchaseRepo purchaseRepo;

    public Page<AdminPurchaseResponseDto> getAllPurchases(Pageable pageable) {
        return purchaseRepo.findAllByOrderByPurchaseTimeDesc(pageable)
                .map(AdminPurchaseResponseDto::from);
    }

    public Page<AdminPurchaseResponseDto> getPurchasesByMemberId(
            Long memberId,
            Pageable pageable
    ) {
        return purchaseRepo.findByMember_IdOrderByPurchaseTimeDesc(
                        memberId,
                        pageable
                )
                .map(AdminPurchaseResponseDto::from);
    }

    public Page<AdminPurchaseResponseDto> getPurchasesByMemberEmail(
            String email,
            Pageable pageable
    ) {
        return purchaseRepo
                .findByMember_EmailContainingIgnoreCaseOrderByPurchaseTimeDesc(
                        email,
                        pageable
                )
                .map(AdminPurchaseResponseDto::from);
    }

    @Transactional
    public void deletePurchase(String purchaseToken) {
        PurchaseEntity purchase = purchaseRepo.findByPurchaseToken(purchaseToken)
                .orElseThrow(() -> new IllegalArgumentException(
                        "결제 내역을 찾을 수 없습니다. purchaseToken=" + purchaseToken
                ));

        purchaseRepo.delete(purchase);
    }
}