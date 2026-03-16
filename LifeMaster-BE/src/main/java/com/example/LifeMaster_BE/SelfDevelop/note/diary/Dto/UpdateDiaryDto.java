package com.example.LifeMaster_BE.SelfDevelop.note.diary.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiaryDto {

    private String diaryContent;
    private LocalDate diaryDate; //추가

    public String getDate() {
        if (diaryDate == null) {
            throw new IllegalStateException("diaryDate가 null입니다.");
        }
        return diaryDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
