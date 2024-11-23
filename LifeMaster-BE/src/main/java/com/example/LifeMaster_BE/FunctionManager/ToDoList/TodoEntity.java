package com.example.LifeMaster_BE.FunctionManager.ToDoList;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Getter와 Setter
    @Column(name = "calendar_date")
    private String date;

    private String title;
    @Nullable
    private String description;
    private boolean completed;

    // 기본 생성자
    public TodoEntity() {
    }

}


