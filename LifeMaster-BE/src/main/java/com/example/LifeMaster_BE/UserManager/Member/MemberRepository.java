package com.example.LifeMaster_BE.UserManager.Member;

import com.example.LifeMaster_BE.Admin.Dto.DailyCountDto;
import com.example.LifeMaster_BE.Admin.Dto.LoginTypeCountDto;
import com.example.LifeMaster_BE.Admin.Dto.MonthlyCountDto;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String Email);

    Optional<MemberEntity> findByEmail(String email);
    @Query("SELECT m.email FROM MemberEntity m WHERE m.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);

    // 관리자용 검색 메서드
    @Query("SELECT m FROM MemberEntity m WHERE " +
            "(:keyword IS NULL OR m.email LIKE %:keyword% OR m.nickname LIKE %:keyword%) AND " +
            "(:status IS NULL OR m.memberStatus = :status)")
    Page<MemberEntity> searchMembers(
            @Param("keyword") String keyword,
            @Param("status") MemberStatus status,
            Pageable pageable);

    // 통계용 메서드
    long countByMemberStatus(MemberStatus status);

    @Query("SELECT COUNT(m) FROM MemberEntity m WHERE m.createdAt >= :startDate")
    long countNewMembersSince(@Param("startDate") LocalDateTime startDate);

    List<MemberEntity> findAllBySubscriptionPlan(SubscriptionPlan subscriptionPlan);

    List<MemberEntity> findAll();

    // 🔥 일별 가입자 수
    @Query("""
        SELECT new com.example.LifeMaster_BE.Admin.Dto.DailyCountDto(
            FUNCTION('DATE', m.createdAt),
            COUNT(m)
        )
        FROM MemberEntity m
        GROUP BY FUNCTION('DATE', m.createdAt)
        ORDER BY FUNCTION('DATE', m.createdAt)
    """)
    List<DailyCountDto> countDailyUsers();


    // 🔥 월별 가입자 수
    @Query("""
        SELECT new com.example.LifeMaster_BE.Admin.Dto.MonthlyCountDto(
            FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m'),
            COUNT(m)
        )
        FROM MemberEntity m
        GROUP BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m')
        ORDER BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m')
    """)
    List<MonthlyCountDto> countMonthlyUsers();


    // 🔥 활성 유저 (최근 7일 로그인 기준 예시)
    @Query("""
        SELECT COUNT(m)
        FROM MemberEntity m
        WHERE m.loginStatus = true
    """)
    Long countActiveUsers();

    @Query("""
    SELECT new com.example.LifeMaster_BE.Admin.Dto.LoginTypeCountDto(
        m.loginType,
        COUNT(m)
    )
    FROM MemberEntity m
    GROUP BY m.loginType
""")
    List<LoginTypeCountDto> countByLoginType();
}


