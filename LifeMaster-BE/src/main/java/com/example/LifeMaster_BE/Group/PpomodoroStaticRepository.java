package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface PpomodoroStaticRepository extends JpaRepository<PpomodoroStatic, Long> {
    Optional<PpomodoroStatic> findByUserAndDate(MemberEntity user, Date date);
}
