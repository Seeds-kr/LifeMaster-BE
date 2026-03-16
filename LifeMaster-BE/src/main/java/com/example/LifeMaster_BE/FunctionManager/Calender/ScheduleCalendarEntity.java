package com.example.LifeMaster_BE.FunctionManager.Calender;


import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_calendar_member_date", columnNames = {"member_id", "date"})
        }
)
public class ScheduleCalendarEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonIgnore
    private MemberEntity member;

    @Column(nullable = false)
    private String date; // yyyyMMdd (더 이상 전역 unique 아님)

    private String day;

    @ElementCollection
    private List<String> events;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("calendar")
    @JsonIgnore
    private List<TodoEntity> todos;
}

