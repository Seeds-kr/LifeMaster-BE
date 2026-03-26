package com.example.LifeMaster_BE.Group.Stats;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.FeatureType;
import com.example.LifeMaster_BE.UserManager.Member.Subscription.SubscriptionAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupStatsService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GoalAchievementRepository goalAchievementRepository;
    private final SubscriptionAccessService subscriptionAccessService;

    public GroupRankingResponseDTO getGroupRanking(Long groupId, Long loginUserId, RankingScope scope) {

        MemberEntity member = getMemberOrThrow(loginUserId);
        // 프리미엄 기능 접근 검사
        subscriptionAccessService.validateFeatureAccess(member, FeatureType.GROUP);

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        MemberEntity loginUser = memberRepository.findById(loginUserId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + loginUserId));

        boolean isMember = group.getMembers()
                .stream()
                .anyMatch(groupMember -> groupMember.getId().equals(loginUserId));

        if (!isMember) {
            throw new RuntimeException("해당 그룹의 멤버만 랭킹을 조회할 수 있습니다.");
        }

        List<Object[]> rawRanks = getRawRanks(groupId, scope);
        List<GroupRankingItemDTO> items = new ArrayList<>();

        int myRank = 0;
        int myAchieveCount = 0;
        String myName = loginUser.getNickname();
        String myProfileImage = loginUser.getImageUrl();

        int previousCount = -1;
        int currentRank = 0;

        for (int i = 0; i < rawRanks.size(); i++) {
            Object[] row = rawRanks.get(i);

            Long memberId = (Long) row[0];
            String nickname = (String) row[1];
            String profileImage = (String) row[2];
            int achieveCount = ((Long) row[3]).intValue();

            if (achieveCount != previousCount) {
                currentRank = i + 1;
                previousCount = achieveCount;
            }

            GroupRankingItemDTO item = new GroupRankingItemDTO(
                    currentRank,
                    memberId,
                    nickname,
                    profileImage,
                    achieveCount
            );
            items.add(item);

            if (memberId.equals(loginUserId)) {
                myRank = currentRank;
                myAchieveCount = achieveCount;
                myName = nickname;
                myProfileImage = profileImage;
            }
        }

        return new GroupRankingResponseDTO(
                scope.name().toLowerCase(),
                myRank,
                loginUserId,
                myName,
                myProfileImage,
                myAchieveCount,
                items
        );
    }

    private List<Object[]> getRawRanks(Long groupId, RankingScope scope) {
        if (scope == RankingScope.WEEKLY) {
            LocalDate today = LocalDate.now(ZONE_ID);
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);

            LocalDateTime startDateTime = startOfWeek.atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusDays(7);

            return goalAchievementRepository.findGroupRankingWeekly(groupId, startDateTime, endDateTime);
        }

        return goalAchievementRepository.findGroupRankingTotal(groupId);
    }

    private MemberEntity getMemberOrThrow(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }
}