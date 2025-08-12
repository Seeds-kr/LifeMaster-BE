package com.example.LifeMaster_BE.SelfDevelop.note.thank.Dto;

import com.example.LifeMaster_BE.SelfDevelop.note.thank.ThankEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
}
