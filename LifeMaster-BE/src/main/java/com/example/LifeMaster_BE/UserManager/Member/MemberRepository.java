package com.example.LifeMaster_BE.UserManager.Member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);
    Optional<MemberEntity> findByNickname(String nickname);
    Optional<MemberEntity> findByEmail(String email);
}
