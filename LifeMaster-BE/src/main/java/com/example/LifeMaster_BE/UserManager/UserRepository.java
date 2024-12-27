package com.example.LifeMaster_BE.UserManager;

import com.example.LifeMaster_BE.Challenge.Detox.Detox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
