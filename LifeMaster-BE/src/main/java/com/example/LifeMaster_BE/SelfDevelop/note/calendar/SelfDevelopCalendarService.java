package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryRepository;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service("selfDevelopCalendarService")
@RequiredArgsConstructor
public class SelfDevelopCalendarService {

    private final DiaryRepository diaryRepository;
    private final ThankRepository thankRepository;

    public CalendarDailyContentDto getEventsByDate(LocalDate date){
        DiaryEntity diary = diaryRepository.findByDiaryDate(date);
        ThankEntity thank = thankRepository.findByThankDate(date);

        return convertToDto(diary, thank);
    }

    public CalendarDailyContentDto convertToDto(DiaryEntity diary, ThankEntity thank) {
        return new CalendarDailyContentDto(diary.getDiaryContent(),
                thank.getThankOne(),
                thank.getThankTwo(),
                thank.getThankThree(),
                thank.getThankFour(),
                thank.getThankFive());
    }
}
