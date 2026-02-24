package com.example.LifeMaster_BE.UserManager.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);
    boolean existsByEmail(String Email);

    Optional<MemberEntity> findByEmail(String email);
    @Query("SELECT m.email FROM MemberEntity m WHERE m.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);
}
