package com.example.LifeMaster_BE.SelfDevelop.note;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class Diary {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime diaryDate;

    @Lob
    private String diaryContent;

    // 추후에 User 엔티티가 추가된다면
    /*
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
     */

}
