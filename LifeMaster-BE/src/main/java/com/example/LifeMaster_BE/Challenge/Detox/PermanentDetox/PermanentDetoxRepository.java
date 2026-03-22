package com.example.LifeMaster_BE.Challenge.Detox.PermanentDetox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermanentDetoxRepository extends JpaRepository<PermanentDetoxEntity, Long> {
    Optional<PermanentDetoxEntity> findByMember_Id(Long memberId);
    boolean existsByMember_Id(Long memberId);
}