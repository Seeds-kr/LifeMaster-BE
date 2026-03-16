package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WhiteNoiseRepository extends JpaRepository<WhiteNoiseEntity, Long> {
    List<WhiteNoiseEntity> findAllByMember(MemberEntity member);

    @Query("SELECT w FROM WhiteNoiseEntity w WHERE w.member IS NULL OR w.member = :member")
    List<WhiteNoiseEntity> findByMemberIsNullOrMember(@Param("member") MemberEntity member);
}

