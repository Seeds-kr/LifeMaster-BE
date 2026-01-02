package com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ThankResponse {
    private String thankOne;
    private String thankTwo;
    private String thankThree;
    private String thankFour;
    private String thankFive;

    private LocalDate thankDate;
}
