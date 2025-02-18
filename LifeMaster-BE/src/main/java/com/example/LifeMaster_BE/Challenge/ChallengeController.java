package com.example.LifeMaster_BE.Challenge;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;

    /** 0. 챌린지 생성 */
    @PostMapping
    public Challenge createChallenge(@RequestBody ChallengeDto.Create challenge) {
        return challengeService.createChallenge(challenge);
    }


    /** 1. 챌린지 목록 조회 */
    @GetMapping
    public Page<Challenge> getAllChallenges(@RequestParam(defaultValue = "0") int page) {
        return challengeService.getAllChallenges(page);
    }

    /** 2. 챌린지 검색 */
    @GetMapping("/search")
    public Page<ChallengeDto.List> searchChallenges(@RequestParam String name, @RequestParam(defaultValue = "0") int page
    , @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.searchChallenges(name, page, userDetails);
    }

    /** 3. 내가 참여한 챌린지 목록 */
    @GetMapping("/my")
    public List<Challenge> getMyChallenges(@AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.getMyChallenges(userDetails);
    }

    /** 4. 특정 챌린지 세부 정보 */
    @GetMapping("/{challId}")
    public Challenge getChallengeDetail(@PathVariable Long challId) {
        return challengeService.getChallengeDetail(challId);
    }

    /** 5. 챌린지 참여 */
    @PostMapping("/{challId}/join")
    public String joinChallenge(@PathVariable Long challId, @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.joinChallenge(challId, userDetails);
    }

    /** 6. 챌린지 참여 취소 */
    @DeleteMapping("/{challId}/leave")
    public String leaveChallenge(@PathVariable Long challId, @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.leaveChallenge(challId, userDetails);
    }

    /** 7. 챌린지 삭제 */
    @DeleteMapping("/{challId}")
    public String deleteChallenge(@PathVariable Long challId) {
        return challengeService.deleteChallenge(challId);
    }
}
