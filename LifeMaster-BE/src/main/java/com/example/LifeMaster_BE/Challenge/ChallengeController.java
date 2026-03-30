package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;
    private final ScheduleCalendarService scheduleCalendarService;

    /** 0. 챌린지 생성 */
    @PostMapping
    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    public Challenge createChallenge(
            @RequestBody ChallengeDto.Create challenge,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();
        String email = user.getUsername(); // 기존 로직 유지

        // ✅ 챌린지 날짜(yyyyMMdd) 기준으로 캘린더 이벤트 추가 (로그인 유저 기준)
        scheduleCalendarService.addOrUpdateEvent(memberId, challenge.getDate(), "Challenge");

        return challengeService.createChallenge(challenge, email);
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
    public List<ChallengeDto.List> getMyChallenges(@AuthenticationPrincipal UserDetails userDetails) {
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
    public String joinChallenge(
            @PathVariable Long challId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long memberId = user.getId();

        // 참여한 '현재 날짜' 기준 (KST)
        String dateKey = LocalDate
                .now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // ✅ 캘린더 이벤트 추가 (로그인 유저 기준)
        scheduleCalendarService.addOrUpdateEvent(memberId, dateKey, "Challenge");

        // 참여 처리 (필요에 따라 challengeService도 CustomUserDetails 받게 바꾸거나,
        // user를 UserDetails로 캐스팅/변환해서 넘기기)
        return challengeService.joinChallenge(challId, user);
    }

    /** 6. 챌린지 참여 취소 */
    @DeleteMapping("/{challId}/leave")
    @Operation(summary = "챌린지 참여 취소", description = "사용자가 특정 챌린지 참여를 취소합니다.")
    public String leaveChallenge(@PathVariable Long challId,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        // memberId 확보
        Long memberId = ((CustomUserDetails) userDetails).getId();

        // 오늘 날짜 (참여 취소는 '행동 로그' → 오늘 기준)
        String today = LocalDate
                .now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 참여 취소 처리
        String result = challengeService.leaveChallenge(challId, userDetails);

        // 오늘 기준, 같은 memberId의 챌린지 참여가 0개면 캘린더 이벤트 삭제
        if (challengeService.countJoinedChallengesOnDate(memberId, today) == 0) {
            scheduleCalendarService.deleteSpecificEvent(memberId,today, "Challenge");
        }

        return result;
    }

    /** 7. 챌린지 삭제 */
    @DeleteMapping("/{challId}")
    @Operation(summary = "챌린지 삭제", description = "특정 챌린지를 삭제합니다.")
    public String deleteChallenge(@PathVariable Long challId,
                                  @AuthenticationPrincipal CustomUserDetails user) {

        Long memberId = user.getId();

        // 1) 삭제 전: 챌린지 조회(본인 것만) + 날짜 확보
        Challenge challenge = challengeService.getChallengeByIdAndMemberId(challId, memberId);

        LocalDate challDate = challenge.getCreatedAt().toLocalDate();
        String dateKey = challDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 2) 챌린지 삭제 (권한 포함)
        String result = challengeService.deleteChallenge(challId, memberId);

        // 3) 같은 memberId + 같은 날짜의 챌린지가 0개면 캘린더 이벤트 삭제
        if (challengeService.countChallengesByMemberIdAndDate(memberId, challDate) == 0) {
            scheduleCalendarService.deleteSpecificEvent(memberId, dateKey, "Challenge");
        }

        return result;
    }
}
