package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class ThankEntity {

    @Id
    @GeneratedValue
    private Long id;

//    @CreatedDate
    private LocalDateTime thankDate;

    @CreatedDate
    private LocalDateTime createdAt;

    private String thankOne;
    private String thankTwo;
    private String thankThree;
    private String thankFour;
    private String thankFive;

    // 추후에 User 엔티티가 추가된다면
    /*
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
     */

}
