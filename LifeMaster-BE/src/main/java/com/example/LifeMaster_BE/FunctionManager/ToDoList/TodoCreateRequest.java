package com.example.LifeMaster_BE.FunctionManager.ToDoList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoCreateRequest {
    private String date;
    private String title;
}