package com.example.LifeMaster_BE.UserManager.Email.Register;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Member;
import java.util.Optional;

public interface RegisterRepository extends JpaRepository<MemberEntity, Long> {

    boolean existsByNickname(String nickname);
    Optional<MemberEntity> findByNickname(String nickname);
}
