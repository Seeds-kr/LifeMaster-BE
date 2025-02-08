package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class DiaryEntity {

    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime diaryDate;

    @Lob
    private String diaryContent;

    // Member
    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;


}
