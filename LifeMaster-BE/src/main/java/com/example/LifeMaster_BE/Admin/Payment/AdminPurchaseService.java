package com.example.LifeMaster_BE.Admin.Payment;

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
}