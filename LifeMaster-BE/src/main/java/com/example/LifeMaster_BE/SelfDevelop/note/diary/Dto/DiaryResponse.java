package com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DiaryResponse {
    private String diaryContent;
    private LocalDate diaryDate;
}
