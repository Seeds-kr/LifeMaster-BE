package com.example.LifeMaster_BE.Community.Vote;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class VoteDTO {
    @Data
    public class PollRequest {
        private String title;
        private LocalDateTime endDate;
        private List<String> options;
    }

    @Data
    public class VoteRequest {
        private Long optionId;
        private String userId;
    }
}
