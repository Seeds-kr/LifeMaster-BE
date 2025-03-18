package com.example.LifeMaster_BE.Challenge;

import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    public Challenge createChallenge(@RequestBody ChallengeDto.Create challenge, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return challengeService.createChallenge(challenge,email);
    }

    /** 1. 챌린지 목록 조회 */
    @GetMapping
    @Operation(summary = "챌린지 목록 조회", description = "모든 챌린지 목록을 페이지네이션하여 조회합니다.")
    public Page<Challenge> getAllChallenges(@RequestParam(name = "page",defaultValue = "0") int page) {
        return challengeService.getAllChallenges(page);
    }

    /** 2. 챌린지 검색 */
    @GetMapping("/search")
    @Operation(summary = "챌린지 검색", description = "이름을 기준으로 챌린지를 검색합니다.")
    public Page<ChallengeDto.List> searchChallenges(@RequestParam String name,
                                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.searchChallenges(name, page, userDetails);
    }

    /** 3. 내가 참여한 챌린지 목록 */
    @GetMapping("/my")
    @Operation(summary = "내 챌린지 목록 조회", description = "내가 참여한 챌린지 목록을 조회합니다.")
    public List<Challenge> getMyChallenges(@AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.getMyChallenges(userDetails);
    }

    /** 4. 특정 챌린지 세부 정보 */
    @GetMapping("/{challId}")
    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다.")
    public Challenge getChallengeDetail(@PathVariable Long challId) {
        return challengeService.getChallengeDetail(challId);
    }

    /** 5. 챌린지 참여 */
    @PostMapping("/{challId}/join")
    @Operation(summary = "챌린지 참여", description = "사용자가 특정 챌린지에 참여합니다.")
    public String joinChallenge(@PathVariable Long challId, @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.joinChallenge(challId, userDetails);
    }

    /** 6. 챌린지 참여 취소 */
    @DeleteMapping("/{challId}/leave")
    @Operation(summary = "챌린지 참여 취소", description = "사용자가 특정 챌린지 참여를 취소합니다.")
    public String leaveChallenge(@PathVariable Long challId, @AuthenticationPrincipal UserDetails userDetails) {
        return challengeService.leaveChallenge(challId, userDetails);
    }

    /** 7. 챌린지 삭제 */
    @DeleteMapping("/{challId}")
    @Operation(summary = "챌린지 삭제", description = "특정 챌린지를 삭제합니다.")
    public String deleteChallenge(@PathVariable Long challId) {
        return challengeService.deleteChallenge(challId);
    }
}
