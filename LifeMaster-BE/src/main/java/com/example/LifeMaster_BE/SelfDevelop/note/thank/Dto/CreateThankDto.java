package com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto;

import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
public class CreateThankDto {

    private String thankOne;
    private String thankTwo;
    private String thankThree;
    private String thankFour;
    private String thankFive;

    private LocalDate thankDate;

    public ThankEntity toEntity(){
        return ThankEntity.builder()
                .thankOne(thankOne)
                .thankTwo(thankTwo)
                .thankThree(thankThree)
                .thankFour(thankFour)
                .thankFive(thankFive)
                .thankDate(thankDate)
                .build();
    }

    public String getDate() {
        if (thankDate == null) {
            throw new IllegalStateException("thankDate가 null입니다.");
        }
        return thankDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
