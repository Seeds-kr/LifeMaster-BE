package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryRepository;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SelfDevelopCalendarServiceTest {

    @Mock
    private DiaryRepository diaryRepository;

    @Mock
    private ThankRepository thankRepository;

    @InjectMocks
    private SelfDevelopCalendarService calendarService;

    @Test
    @DisplayName("특정 날짜의 일기와 5감사를 DTO로 변환하여 반환한다")
    void getEventsByDate_returnsDto() {

        LocalDate date = LocalDate.of(2025, 8, 13);

        DiaryEntity diary = mock(DiaryEntity.class);
        when(diary.getDiaryContent()).thenReturn("오늘의 일기");

        ThankEntity thank = mock(ThankEntity.class);
        when(thank.getThankOne()).thenReturn("가족에게 감사");
        when(thank.getThankTwo()).thenReturn("건강에 감사");
        when(thank.getThankThree()).thenReturn("친구에 감사");
        when(thank.getThankFour()).thenReturn("일에 감사");
        when(thank.getThankFive()).thenReturn("배움에 감사");

        when(diaryRepository.findByDiaryDate(date)).thenReturn(Optional.of(diary));
        when(thankRepository.findByThankDate(date)).thenReturn(Optional.of(thank));

        CalendarDailyContentDto dto = calendarService.getEventsByDate(date);

        assertNotNull(dto);
        assertEquals("오늘의 일기", dto.getDiaryContent());
        assertEquals("가족에게 감사", dto.getThankOne());
        assertEquals("건강에 감사", dto.getThankTwo());
        assertEquals("친구에 감사", dto.getThankThree());
        assertEquals("일에 감사", dto.getThankFour());
        assertEquals("배움에 감사", dto.getThankFive());

        verify(diaryRepository).findByDiaryDate(date);
        verify(thankRepository).findByThankDate(date);
        verifyNoMoreInteractions(diaryRepository, thankRepository);
    }

    @Test
    @DisplayName("일기 데이터가 없으면 EntityNotFoundException 발생")
    void getEventsByDate_throwsWhenDiaryNotFound() {
        LocalDate date = LocalDate.of(2025, 8, 13);

        when(diaryRepository.findByDiaryDate(date)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> calendarService.getEventsByDate(date));
    }

    @Test
    @DisplayName("5감사 데이터가 없으면 EntityNotFoundException 발생")
    void getEventsByDate_throwsWhenThankNotFound() {
        LocalDate date = LocalDate.of(2025, 8, 13);

        when(diaryRepository.findByDiaryDate(date)).thenReturn(Optional.of(mock(DiaryEntity.class)));
        when(thankRepository.findByThankDate(date)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> calendarService.getEventsByDate(date));
    }

}