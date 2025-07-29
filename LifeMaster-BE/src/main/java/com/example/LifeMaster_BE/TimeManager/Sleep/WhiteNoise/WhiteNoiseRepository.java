package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WhiteNoiseRepository extends JpaRepository<WhiteNoiseEntity, Long> {
    List<WhiteNoiseEntity> findAllByMember(MemberEntity member);
}

