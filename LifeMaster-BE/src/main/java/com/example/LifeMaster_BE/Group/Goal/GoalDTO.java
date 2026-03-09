package com.example.LifeMaster_BE.Group.Goal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GoalDTO {

    private String name;

    private GoalCondition goalCondition; // TIME / COUNT

    private int value;

    private GoalDuration duration; // DAILY / WEEKLY / MONTHLY

    private Long groupId;
}