package com.example.LifeMaster_BE.Group;

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
    private String goalCondition;
    private int value;
    private int duration;
    private Long groupId;

    // Getters and Setters
}
