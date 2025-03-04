package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SleepRepository extends JpaRepository<Sleep, Integer> {
    List<Sleep> findByUserAndSleepDateAfter(MemberEntity User, LocalDateTime oneWeekAgo);
}
