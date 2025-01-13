package com.example.LifeMaster_BE.Community.Vote;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public class VoteController {
    @RestController
    @RequestMapping("/community/improvePost/poll")
    public static class PollController {

        private final VoteService voteService;

        public PollController(VoteService voteService) {
            this.voteService = voteService;
        }

        //투표 생성
        @Operation(summary = "새 투표 생성", description = "새로운 투표를 생성합니다.")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "투표 생성 성공",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoteEntity.Poll.class))),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content),
                @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
        })
        @PostMapping
        public ResponseEntity<VoteEntity.Poll> createPoll(@RequestBody VoteDTO.PollRequest request) {
            VoteEntity.Poll poll = voteService.createPoll(request.getTitle(), request.getEndDate(), request.getOptions());
            return ResponseEntity.ok(poll);
        }

        //투표
        @Operation(summary = "투표", description = "투표 항목에 투표합니다.")
        @PostMapping("/{pollId}/vote")
        public ResponseEntity<Void> castVote(@PathVariable("pollId") Long pollId, @RequestBody VoteDTO.VoteRequest request) {
            voteService.castVote(pollId, request.getOptionId(), request.getUserId());
            return ResponseEntity.ok().build();
        }

        //항목별 투표자 비율
        @Operation(summary = "투표 결과", description = "투표의 항목별 투표 인원/ 투표 비율을 확인합니다.")
        @GetMapping("/{pollId}/results")
        public ResponseEntity<Map<String, Map<String, Object>>> getPollResults(@PathVariable("pollId") Long pollId) {
            Map<String, Map<String, Object>> results = voteService.getPollResults(pollId);
            return ResponseEntity.ok(results);
        }

        // 투표 주제, 항목 수정
        @Operation(summary = "투표 제목 수정", description = "투표 제목을 수정합니다.")
        @PutMapping("/{pollId}/title")
        public ResponseEntity<VoteEntity.Poll> updatePollTitle(@PathVariable("pollId") Long pollId, @RequestBody VoteDTO.PollTitleRequest request) {
            VoteEntity.Poll updatedPoll = voteService.updatePollTitle(pollId, request.getTitle());
            return ResponseEntity.ok(updatedPoll);
        }

        @Operation(summary = "투표 항목 수정", description = "투표 항목을 수정합니다.")
        @PutMapping("/{pollId}/options/{optionId}")
        public ResponseEntity<VoteEntity.PollOption> updatePollOption(@PathVariable("pollId") Long pollId, @PathVariable("optionId") Long optionId, @RequestBody VoteDTO.PollOptionRequest request) {
            VoteEntity.PollOption updatedOption = voteService.updatePollOption(pollId, optionId, request.getContent());
            return ResponseEntity.ok(updatedOption);
        }

        // 투표 삭제
        @Operation(summary = "투표 삭제", description = "지정된 투표를 삭제합니다.")
        @DeleteMapping("/{pollId}")
        public ResponseEntity<Void> deletePoll(@PathVariable("pollId") Long pollId) {
            voteService.deletePoll(pollId);
            return ResponseEntity.noContent().build();
        }

        // 유효 기간이 지난 투표 삭제
        @Operation(summary = "유효 기간 지난 투표 삭제", description = "유효 기간이 지난 투표를 삭제합니다.")
        @DeleteMapping("/expired")
        public ResponseEntity<Void> deleteExpiredPolls() {
            voteService.deleteExpiredPolls();
            return ResponseEntity.noContent().build();
        }

        @Operation(summary = "투표 목록 확인", description = "진행/만료된 모든 투표 항목을 확인합니다.")
        @GetMapping("/all")
        public ResponseEntity<List<Map<String, Object>>> getAllPolls() {
            List<Map<String, Object>> polls = voteService.getAllPollsWithStatus();
            return ResponseEntity.ok(polls);
        }
    }
}
