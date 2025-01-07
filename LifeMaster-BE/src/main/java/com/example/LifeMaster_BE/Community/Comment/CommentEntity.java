package com.example.LifeMaster_BE.Community.Comment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 설정
public class CommentEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 50)
    private String comment;

    // 작성자 연결

    // 게시글 연결

    @CreatedDate
    private LocalDateTime commentDate;

    public void updateComment(String newComment) {
        if (newComment == null || newComment.isBlank()){
            throw new IllegalArgumentException("Comment cannot be blank");
        }

        this.comment = newComment;
    }
    public CommentEntity(String comment) {
        this.comment = comment;
    }
}
