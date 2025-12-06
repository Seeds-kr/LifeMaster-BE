package com.example.LifeMaster_BE.Community.Vote;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VoteEntity {
    @Entity
    @Getter
    @Setter
    public static class Poll {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false, unique = true)
        private String title;

        private LocalDateTime endDate;

        @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
        @JsonManagedReference // 직렬화에서 이 방향만 포함
        private List<PollOption> options = new ArrayList<>();
    }

    @Entity
    @Getter
    @Setter
    @Table(
            uniqueConstraints = @UniqueConstraint(columnNames = {"poll_id", "content"}) // pollId와 content의 조합에 대해 유니크 제약 추가
    )
    public static class PollOption {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "poll_id")
        @JsonBackReference // 직렬화에서 제외
        private Poll poll;

        private String content;

        private int votes = 0;
    }

    @Entity
    @Table(name = "vote_entity$vote")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OldVote {   // ← 여기 static 추가!!

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "poll_id")
        private Long pollId;

        private String userId;
    }
}
