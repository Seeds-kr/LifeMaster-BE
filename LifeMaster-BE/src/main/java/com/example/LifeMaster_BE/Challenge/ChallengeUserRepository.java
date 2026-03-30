package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeUserRepository extends JpaRepository<ChallengeUser, Long> {
    @Query("SELECT cu FROM ChallengeUser cu JOIN FETCH cu.challenge WHERE cu.user = :user")
    List<ChallengeUser> findByUser(@Param("user") MemberEntity user);
    Optional<ChallengeUser> findByUserAndChallenge(MemberEntity user, Challenge challenge);
    void deleteByChallenge(Challenge challenge);
    boolean existsByChallengeAndUser(Challenge challenge, MemberEntity user);

    long countByUser(MemberEntity user);
}
