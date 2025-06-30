package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoResponse {
    private Long id;
    private String date;
    private String title;
    private boolean completed;
}
