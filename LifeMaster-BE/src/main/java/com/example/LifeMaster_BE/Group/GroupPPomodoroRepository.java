package com.example.LifeMaster_BE.Group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface GroupPPomodoroRepository extends JpaRepository<GroupPPomodoro, Long> {
    Optional<GroupPPomodoro> findByGroupAndDate(GroupEntity group, Date date);
}
