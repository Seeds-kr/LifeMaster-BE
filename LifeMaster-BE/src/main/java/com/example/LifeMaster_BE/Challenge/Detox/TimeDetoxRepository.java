package com.example.LifeMaster_BE.Challenge.Detox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeDetoxRepository extends JpaRepository<TimeDetoxEntity, Long> {
    List<TimeDetoxEntity> findByIsActiveTrue();
}