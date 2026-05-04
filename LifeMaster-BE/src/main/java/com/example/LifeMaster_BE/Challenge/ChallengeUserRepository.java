package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChallengeUserRepository extends JpaRepository<ChallengeUser, Long> {
    List<ChallengeUser> findByUser(MemberEntity user);
    Optional<ChallengeUser> findByUserAndChallenge(MemberEntity user, Challenge challenge);
    void deleteByChallenge(Challenge challenge);
    boolean existsByChallengeAndUser(Challenge challenge, MemberEntity user);

    long countByUser(MemberEntity user);

    long countByUserId(Long userId);

    @Query("""
    SELECT COUNT(cu)
    FROM ChallengeUser cu
""")
    Long countTotalParticipants();
}
