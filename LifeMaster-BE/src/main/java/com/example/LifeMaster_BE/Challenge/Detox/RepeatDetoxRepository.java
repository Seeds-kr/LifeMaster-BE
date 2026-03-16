package com.example.LifeMaster_BE.Challenge.Detox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepeatDetoxRepository extends JpaRepository<RepeatDetox, Long> {

}