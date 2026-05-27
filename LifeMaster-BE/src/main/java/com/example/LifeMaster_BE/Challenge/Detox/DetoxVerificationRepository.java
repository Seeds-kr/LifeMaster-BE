package com.example.LifeMaster_BE.Challenge.Detox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DetoxVerificationRepository extends JpaRepository<DetoxVerificationEntity, Long> {

    Optional<DetoxVerificationEntity> findByMember_IdAndUsedFalse(Long memberId);

    Optional<DetoxVerificationEntity> findByMember_Id(Long memberId);
}
