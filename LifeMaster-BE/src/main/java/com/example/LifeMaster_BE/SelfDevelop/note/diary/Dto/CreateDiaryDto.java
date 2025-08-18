package com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto;

import com.example.LifeMaster_BE.SelfDevelop.note.diary.DiaryEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateDiaryDto {

    private String diaryContent;
    private LocalDate diaryDate;

    public DiaryEntity toEntity() {
        return DiaryEntity.builder()
                .diaryContent(diaryContent)
                .diaryDate(diaryDate)
                .build();
    }
}
