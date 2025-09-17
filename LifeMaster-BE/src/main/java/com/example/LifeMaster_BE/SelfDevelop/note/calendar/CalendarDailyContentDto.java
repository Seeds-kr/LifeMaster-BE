package com.example.LifeMaster_BE.SelfDevelop.note.calendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDailyContentDto {

    private Long diaryId;
    private Long thankId;
    private String diaryContent;
    private String thankOne;
    private String thankTwo;
    private String thankThree;
    private String thankFour;
    private String thankFive;
}
