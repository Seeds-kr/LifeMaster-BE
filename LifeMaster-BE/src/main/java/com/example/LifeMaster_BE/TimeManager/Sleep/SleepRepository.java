package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SleepRepository extends JpaRepository<Sleep, Integer> {

    // 최근 수면 기록 조회
    List<Sleep> findByUserAndSleepDateAfter(
            MemberEntity user,
            LocalDate oneWeekAgo
    );

    // 사용자 수면 기록 1건 조회
    Sleep findByUser(MemberEntity user);

    // 여러 사용자의 전체 수면 기록 조회
    @Query("SELECT s FROM Sleep s WHERE s.user IN :users")
    List<Sleep> findAllByUser(
            @Param("users") List<MemberEntity> users
    );

    // 특정 사용자의 특정 날짜 수면 기록 조회
    @Query("""
            SELECT s
            FROM Sleep s
            WHERE s.user = :user
              AND DATE(s.sleepStart) = :date
            """)
    List<Sleep> findByUserAndDate(
            @Param("user") MemberEntity user,
            @Param("date") LocalDate date
    );

    // 여러 사용자의 특정 날짜 수면 기록 조회
    @Query("""
            SELECT s
            FROM Sleep s
            WHERE s.user IN :users
              AND DATE(s.sleepStart) = :date
            """)
    List<Sleep> findAllByUserAndDate(
            @Param("users") List<MemberEntity> users,
            @Param("date") LocalDate date
    );

    // ==============================
    // 그룹 목표 통계용
    // ==============================

    /**
     * 특정 사용자의 기간 내 수면 기록 조회
     *
     * DAILY   -> 오늘 ~ 오늘
     * WEEKLY  -> 월요일 ~ 일요일
     * MONTHLY -> 1일 ~ 말일
     */
    @Query("""
            SELECT s
            FROM Sleep s
            WHERE s.user = :user
              AND s.sleepDate BETWEEN :startDate AND :endDate
            ORDER BY s.sleepDate ASC
            """)
    List<Sleep> findByUserAndSleepDateBetween(
            @Param("user") MemberEntity user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 여러 사용자의 기간 내 수면 기록 조회
     *
     * 나중에 그룹 전체 SLEEP 통계를
     * 한 번의 쿼리로 계산하고 싶을 때 사용 가능
     */
    @Query("""
            SELECT s
            FROM Sleep s
            WHERE s.user IN :users
              AND s.sleepDate BETWEEN :startDate AND :endDate
            ORDER BY s.sleepDate ASC
            """)
    List<Sleep> findAllByUsersAndSleepDateBetween(
            @Param("users") List<MemberEntity> users,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}