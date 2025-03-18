package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoDTO {
    private String date;
    private String title;
    private boolean completed;
}