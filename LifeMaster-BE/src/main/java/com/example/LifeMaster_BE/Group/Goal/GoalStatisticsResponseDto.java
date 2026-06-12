package com.example.LifeMaster_BE.Group.Goal;

import com.example.LifeMaster_BE.Group.Goal.GoalType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GoalStatisticsResponseDto {

    private Long goalId;
    private GoalType goalType;
    private List<Double> userValues;
    private List<Double> groupAverageValues;
}