package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepeatDetoxRepository extends JpaRepository<RepeatDetox, Long> {
    List<RepeatDetox> findByMember_Id(Long userId);
}