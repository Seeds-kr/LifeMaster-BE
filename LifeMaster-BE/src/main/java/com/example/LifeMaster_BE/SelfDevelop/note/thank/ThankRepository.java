package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ThankRepository extends JpaRepository<ThankEntity, Long> {

    ThankEntity findByThankDate(LocalDate date);
}
