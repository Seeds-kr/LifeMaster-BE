package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ThankRepository extends JpaRepository<ThankEntity, Long> {

    Optional<ThankEntity> findByThankDate(LocalDate date);
}
