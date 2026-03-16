package com.example.LifeMaster_BE.Community.Vote;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class VoteDTO {
    @Data
    public static class PollRequest {
        private String title;
        private LocalDateTime endDate;
        private List<String> options;
    }

    @Data
    public static class VoteRequest {
        private Long optionId;
        private String userId;
    }

    @Data
    public static class PollOptionRequest {
        private String content;
        // getters and setters
    }

    @Data
    public static class PollTitleRequest {
        private String title;

        // getters and setters
    }
}
