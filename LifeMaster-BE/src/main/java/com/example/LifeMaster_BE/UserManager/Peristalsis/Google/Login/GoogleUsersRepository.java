package com.example.LifeMaster_BE.UserManager.Peristalsis.Google.Login;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleUsersRepository extends JpaRepository<GoogleUsersEntity, Long> {
    Optional<GoogleUsersEntity> findByEmail(String email); // 이메일을 기반으로 사용자 찾기

    Optional<GoogleUsersEntity> findByIdentifier(String id);
}

