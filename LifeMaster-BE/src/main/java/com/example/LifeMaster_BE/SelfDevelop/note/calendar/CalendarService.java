package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryRepository;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final DiaryRepository diaryRepository;
    private final ThankRepository thankRepository;

    public EventDto getEventsByDate(LocalDateTime date){
        DiaryEntity diary = diaryRepository.findByDiaryDate(date);
        ThankEntity thank = thankRepository.findByThankDate(date);

        return convertToDto(diary, thank);
    }

    public EventDto convertToDto(DiaryEntity diary, ThankEntity thank) {
        return new EventDto(diary.getDiaryContent(),
                thank.getThankOne(),
                thank.getThankTwo(),
                thank.getThankThree(),
                thank.getThankFour(),
                thank.getThankFive());
    }
}
