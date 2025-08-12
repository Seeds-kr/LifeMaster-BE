package com.example.LifeMaster_BE.SelfDevelop.note.diary;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class DiaryEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    private String diaryContent;

    private LocalDate diaryDate;

    @CreatedDate
    private LocalDateTime createdAt;


    // Member
    @ManyToOne
    @JoinColumn(name = "member_id")
    private MemberEntity member;


}
