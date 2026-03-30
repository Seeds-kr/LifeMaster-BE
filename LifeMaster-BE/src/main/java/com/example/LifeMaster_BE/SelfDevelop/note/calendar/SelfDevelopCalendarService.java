package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryRepository;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service("selfDevelopCalendarService")
@Transactional
@RequiredArgsConstructor
public class SelfDevelopCalendarService {

    private final DiaryRepository diaryRepository;
    private final ThankRepository thankRepository;

    public CalendarDailyContentDto getEventsByDate(LocalDate date){

        Optional<DiaryEntity> diary = diaryRepository.findByDiaryDate(date);
        Optional<ThankEntity> thank = thankRepository.findByThankDate(date);

        if (diary.isEmpty() && thank.isEmpty()) {
            throw new EntityNotFoundException("해당 날짜에 작성된 일기 또는 감사 기록이 없습니다.");
        }

        return convertToDto(diary.orElse(null), thank.orElse(null));
    }

    public CalendarDailyContentDto convertToDto(DiaryEntity diary, ThankEntity thank) {
        return new CalendarDailyContentDto(
                diary != null ? diary.getId() : null,
                thank != null ? thank.getId() : null,
                diary != null ? diary.getDiaryContent() : null,
                thank != null ? thank.getThankOne() : null,
                thank != null ? thank.getThankTwo() : null,
                thank != null ? thank.getThankThree() : null,
                thank != null ? thank.getThankFour() : null,
                thank != null ? thank.getThankFive() : null);
    }
}
