package com.example.LifeMaster_BE.UserManager.Peristalsis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//연동 로그인 계정의 유저 정보를 저장
public interface OAuthUsersRepository extends JpaRepository<OAuthUsersEntity, Long> {
    Optional<OAuthUsersEntity> findByEmail(String email); // 이메일을 기반으로 사용자 찾기

    Optional<OAuthUsersEntity> findByIdentifier(String id);

    boolean existsByEmail(String email);
}

