package com.example.LifeMaster_BE.Challenge.Detox;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeDetoxRepository extends JpaRepository<TimeDetoxEntity, Long> {
    List<TimeDetoxEntity> findByIsActiveTrue();

    List<TimeDetoxEntity> findAllByMember(MemberEntity member);
    List<TimeDetoxEntity> findByMember_IdAndIsActiveTrue(Long memberId);

    Optional<TimeDetoxEntity> findByIdAndMember_Id(Long id, Long memberId);

    List<TimeDetoxEntity> findAllByMember_Id(Long memberId);
}