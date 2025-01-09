package com.example.LifeMaster_BE.Community.Vote;

import jakarta.persistence.*;
import lombok.Getter;
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

        private String title;

        private LocalDateTime endDate;

        @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
        private List<PollOption> options = new ArrayList<>();
    }

    @Entity
    @Getter
    @Setter
    public static class PollOption {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "poll_id")
        private Poll poll;

        private String content;

        private int votes = 0;
    }

    @Entity
    @Getter
    @Setter
    public static class Vote {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "poll_id")
        private Poll poll;

        private String userId;
    }
}
