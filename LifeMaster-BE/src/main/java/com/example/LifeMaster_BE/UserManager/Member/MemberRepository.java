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

    // 탈퇴하지 않은 회원 조회 메서드 (일반 사용자용)
    boolean existsByNicknameAndMemberStatusNot(String nickname, MemberStatus status);
    boolean existsByEmailAndMemberStatusNot(String email, MemberStatus status);
    Optional<MemberEntity> findByEmailAndMemberStatusNot(String email, MemberStatus status);

    // 기존 메서드 (관리자용 - 탈퇴 회원 포함)
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

    Page<MemberEntity> findAllBySubscriptionPlan(
            SubscriptionPlan subscriptionPlan,
            Pageable pageable
    );

    List<MemberEntity> findAll();

    @Query("""
SELECT new com.example.LifeMaster_BE.Admin.Dto.DailyCountDto(
    FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m-%d'),
    COUNT(m)
)
FROM MemberEntity m
GROUP BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m-%d')
ORDER BY FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m-%d')
""")
    List<DailyCountDto> countDailyUsers();


    @Query("""
SELECT new com.example.LifeMaster_BE.Admin.Dto.MonthlyCountDto(
    YEAR(m.createdAt),
    MONTH(m.createdAt),
    COUNT(m)
)
FROM MemberEntity m
GROUP BY YEAR(m.createdAt), MONTH(m.createdAt)
ORDER BY YEAR(m.createdAt), MONTH(m.createdAt)
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

    Page<MemberEntity> findAll(Pageable pageable);

    long countBySubscriptionPlan(SubscriptionPlan subscriptionPlan);

    long countByLoginRole(LoginRole loginRole);
}


