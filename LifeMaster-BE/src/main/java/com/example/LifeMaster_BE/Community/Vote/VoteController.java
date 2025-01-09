package com.example.LifeMaster_BE.Community.Vote;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public class VoteController {
    @RestController
    @RequestMapping("/community/improvePost/poll")
    public static class PollController {

        private final VoteService voteService;

        public PollController(VoteService voteService) {
            this.voteService = voteService;
        }

        @PostMapping
        public ResponseEntity<VoteEntity.Poll> createPoll(@RequestBody VoteDTO.PollRequest request) {
            VoteEntity.Poll poll = voteService.createPoll(request.getTitle(), request.getEndDate(), request.getOptions());
            return ResponseEntity.ok(poll);
        }

        @PostMapping("/{pollId}/vote")
        public ResponseEntity<Void> castVote(@PathVariable Long pollId, @RequestBody VoteDTO.VoteRequest request) {
            voteService.castVote(pollId, request.getOptionId(), request.getUserId());
            return ResponseEntity.ok().build();
        }

        @GetMapping("/{pollId}/results")
        public ResponseEntity<Map<String, Double>> getPollResults(@PathVariable Long pollId) {
            return ResponseEntity.ok(voteService.getPollResults(pollId));
        }
    }
}
