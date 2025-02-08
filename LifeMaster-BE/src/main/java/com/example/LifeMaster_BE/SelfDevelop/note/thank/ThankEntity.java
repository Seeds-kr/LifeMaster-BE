package com.example.LifeMaster_BE.SelfDevelop.note.thank;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
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

    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;

}
