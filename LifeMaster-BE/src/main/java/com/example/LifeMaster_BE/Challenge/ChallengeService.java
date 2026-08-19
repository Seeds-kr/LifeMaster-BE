package com.example.LifeMaster_BE.Challenge;

import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeUserRepository challengeUserRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionAccessService subscriptionAccessService;
    private final ChallengeCompletionRepository challengeCompletionRepository;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final ApplicationEventPublisher eventPublisher;

    // 챌린지 생성
    public Challenge createChallenge(ChallengeDto.Create challengeDto, String email) {
        MemberEntity user = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        Challenge challenge = Challenge.builder()
                .challName(challengeDto.getChallName())
                .challDesc(challengeDto.getChallDesc())
                .challImg(challengeDto.getChallImg())
                .user(user)
                .challCnt(1)
                .build();
        return challengeRepository.save(challenge);
    }

    /** 1. 챌린지 전체 목록 (페이징) */
    public Page<Challenge> getAllChallenges(int page) {
        return challengeRepository.findAll(PageRequest.of(page, 20));
    }

    /** 2. 챌린지 검색 (이름 기준) */
    public Page<ChallengeDto.List> searchChallenges(String name, int page, @AuthenticationPrincipal UserDetails userDetails) {
        MemberEntity user = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Page<Challenge> pageChallenge = challengeRepository.findByChallNameContaining(
                name, PageRequest.of(page, 20)
        );

        return pageChallenge.map(challenge -> {
            boolean isUserParticipating = challengeUserRepository.existsByChallengeAndUser(challenge, user);

            return ChallengeDto.List.builder()
                    .challId(challenge.getChallId().intValue())
                    .challName(challenge.getChallName())
                    .challDesc(challenge.getChallDesc())
                    .challImg(challenge.getChallImg())
                    .challMe(isUserParticipating)  // 🔥 본인 참여 여부 반영
                    .challCnt(challenge.getChallCnt())
                    .build();
        });
    }


    /** 3. 내가 참여한 챌린지 목록 */
    public List<ChallengeDto.List> getMyChallenges(UserDetails userDetails) {

        MemberEntity user = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        List<ChallengeUser> challengeUsers = challengeUserRepository.findByUser(user);

        return challengeUsers.stream()
                .map(cu -> ChallengeDto.List.builder()
                        .challId(Math.toIntExact(cu.getChallenge().getChallId()))
                        .challName(cu.getChallenge().getChallName())
                        .challDesc(cu.getChallenge().getChallDesc())
                        .challImg(cu.getChallenge().getChallImg())
                        .challMe(true)
                        .challCnt(cu.getChallenge().getChallCnt())
                        .build())
                .toList();
    }



    /** 4. 특정 챌린지 상세 정보 조회 */
    public Challenge getChallengeDetail(Long challId) {
        return challengeRepository.findById(challId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));
    }

    /** 5. 챌린지 참여 */
    @Transactional
    public String joinChallenge(Long challId, @AuthenticationPrincipal CustomUserDetails userDetails) {

        MemberEntity member = getMemberOrThrow(userDetails.getId());

        MemberEntity user = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Challenge challenge = challengeRepository.findById(challId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        if (challengeUserRepository.findByUserAndChallenge(user, challenge).isPresent()) {
            return "이미 참여 중인 챌린지입니다.";
        }

        // 현재 사용자가 참여 중인 챌린지 개수 확인
        long joinedChallengeCount = challengeUserRepository.countByUser(user);

        // 이미 1개 이상 참여 중이면, 추가 참여는 프리미엄만 가능
        if (joinedChallengeCount >= 1) {
            subscriptionAccessService.validateFeatureAccess(member, FeatureType.MULTI_CHALLENGE);
        }

        Challenge updatedChallenge = challenge.toBuilder()
                .challCnt(challenge.getChallCnt() + 1)
                .build();

        ChallengeUser challengeUser = ChallengeUser.builder()
                .challenge(updatedChallenge)
                .user(user)
                .build();

        challengeRepository.save(updatedChallenge);
        challengeUserRepository.save(challengeUser);

        return "챌린지 참여 완료!";
    }

    /** 6. 챌린지 참여 취소 */
    @Transactional
    public String leaveChallenge(Long challId, @AuthenticationPrincipal UserDetails userDetails) {
        MemberEntity user = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        Challenge challenge = challengeRepository.findById(challId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        ChallengeUser challengeUser = challengeUserRepository.findByUserAndChallenge(user, challenge)
                .orElseThrow(() -> new RuntimeException("참여하지 않은 챌린지입니다."));

        int updatedCnt = Math.max(challenge.getChallCnt() - 1, 0); // 최소 0 유지

        Challenge updatedChallenge = challenge.toBuilder()
                .challCnt(updatedCnt)
                .build();

        challengeRepository.save(updatedChallenge);

        challengeUserRepository.delete(challengeUser);

        return "챌린지 참여 취소 완료!";
    }


    /** 7. 챌린지 삭제 */
    @Transactional
    public String deleteChallenge(Long challId) {
        Challenge challenge = challengeRepository.findById(challId)
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        // 해당 챌린지에 속한 참여 정보 삭제
        challengeUserRepository.deleteByChallenge(challenge);
        challengeRepository.delete(challenge);

        return "챌린지 삭제 완료!";
    }


    // 추천 챌린지 알림을 매일 일정 시각에 보내기
    @Scheduled(cron = "0 0 12 * * ?") // 매일 오전 12시에 실행
    public void sendDailyChallengeNotification() {
        // 추천할 챌린지 목록 가져오기 (예: 최신 챌린지나 특정 조건에 맞는 챌린지)
        List<Challenge> recommendedChallenges = challengeRepository.findTop5ByOrderByCreatedAtDesc(); // 예시로 최신 챌린지 5개

        // 모든 유저에게 추천 챌린지 알림 보내기
        List<MemberEntity> users = memberRepository.findAll();
        for (MemberEntity user : users) {
            // 유저에게 추천 챌린지 알림 보내기
            sendRecommendationNotification(user, recommendedChallenges);
        }
    }

    // 유저에게 추천 챌린지 알림 보내기
    private void sendRecommendationNotification(MemberEntity user, List<Challenge> recommendedChallenges) {
        SseEmitter emitter = emitters.get(user.getId()); // 유저 ID에 맞는 SSE 연결을 가져옵니다.

        if (emitter != null) {
            try {
                // 추천 챌린지를 유저에게 전송
                StringBuilder message = new StringBuilder("추천 챌린지:\n");
                for (Challenge challenge : recommendedChallenges) {
                    message.append("- ").append(challenge.getChallName()).append("\n");
                }
                emitter.send(SseEmitter.event().name("daily-recommendation").data(message.toString()));
            } catch (Exception e) {
                emitters.remove(user.getId());
            }
        }
    }

    // 유저가 챌린지에 참여했을 때 실시간 알림을 받을 수 있도록 SSE 연결을 설정하는 메소드
    public SseEmitter connectToChallengeNotifications(Long userId) {
        SseEmitter emitter = new SseEmitter();
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        return emitter;
    }

    public LocalDate getChallengeDate(Long challId) {
        Challenge challenge = challengeRepository.findById(challId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found: " + challId));

        if (challenge.getCreatedAt() == null) {
            throw new IllegalStateException("challDate가 null입니다. challId=" + challId);
        }
        return challenge.getCreatedAt().toLocalDate(); // LocalDate
    }

    public long countJoinedChallengesOnDate(Long memberId, String yyyyMMdd) {
        LocalDate date = LocalDate.parse(
                yyyyMMdd,
                DateTimeFormatter.ofPattern("yyyyMMdd")
        );

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return challengeRepository
                .countByUser_IdAndCreatedAtBetween(memberId, start, end);
    }


    public Challenge getChallengeByIdAndMemberId(Long challId, Long memberId) {
        return challengeRepository.findByChallIdAndUser_Id(challId, memberId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
    }

    public String deleteChallenge(Long challId, Long memberId) {
        Challenge challenge = getChallengeByIdAndMemberId(challId, memberId);
        challengeRepository.delete(challenge);
        return "Challenge deleted";
    }

    public long countChallengesByMemberIdAndDate(Long memberId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return challengeRepository
                .countByUser_IdAndCreatedAtBetween(memberId, start, end);
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Transactional
    public ChallengeDto.CompleteResponse completeChallenge(
            Long challId,
            CustomUserDetails user
    ) {
        Long memberId = user.getId();

        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        Challenge challenge = challengeRepository.findById(challId)
                .orElseThrow(() -> new RuntimeException("챌린지 없음"));

        LocalDate todayDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String today = todayDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        if (challengeCompletionRepository
                .existsByUserIdAndChallenge_ChallIdAndDateKey(
                        memberId,
                        challId,
                        today
                )) {
            throw new RuntimeException("이미 오늘 완료한 챌린지입니다.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        ChallengeCompletion completion = ChallengeCompletion.builder()
                .user(member)
                .challenge(challenge)
                .completedAt(now)
                .dateKey(today)
                .build();

        challengeCompletionRepository.save(completion);

        eventPublisher.publishEvent(
                new ChallengeProgressChangedEvent(memberId, todayDate)
        );

        return ChallengeDto.CompleteResponse.builder()
                .challId(challId)
                .completedAt(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
                .build();
    }

    public List<ChallengeDto.CompleteStatusResponse> getTodayCompletedChallenges(
            CustomUserDetails user
    ) {

        Long memberId = user.getId();

        String today = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 오늘 완료한 기록
        List<ChallengeCompletion> completions =
                challengeCompletionRepository.findByUser_IdAndDateKey(
                        memberId,
                        today
                );

        return completions.stream()
                .map(completion -> {

                    String completedTime =
                            completion.getCompletedAt()
                                    .format(DateTimeFormatter.ofPattern("HH:mm"));

                    return ChallengeDto.CompleteStatusResponse.builder()
                            .challId(completion.getChallenge().getChallId())
                            .completed(true)
                            .completedAt(completedTime)
                            .build();
                })
                .toList();
    }
}
