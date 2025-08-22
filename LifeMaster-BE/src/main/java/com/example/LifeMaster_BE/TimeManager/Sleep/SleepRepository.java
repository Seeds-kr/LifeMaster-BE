package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface SleepRepository extends JpaRepository<Sleep, Integer> {
    List<Sleep> findByUserAndSleepDateAfter(MemberEntity User, LocalDate oneWeekAgo);

    Sleep findByUser(MemberEntity user);

    @Query("SELECT s FROM Sleep s WHERE s.user IN :users")
    List<Sleep> findAllByUser(List<MemberEntity> users);

    @Query("SELECT s FROM Sleep s WHERE s.user = :user AND DATE(s.sleepStart) = :date")
    List<Sleep> findByUserAndDate(MemberEntity user, LocalDate date);

    @Query("SELECT s FROM Sleep s WHERE s.user IN :users AND DATE(s.sleepStart) = :date")
    List<Sleep> findAllByUserAndDate(List<MemberEntity> users, LocalDate date);
}
