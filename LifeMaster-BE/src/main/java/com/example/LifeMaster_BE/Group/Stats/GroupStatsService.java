package com.example.LifeMaster_BE.Group.Stats;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupStatsService {

    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GoalProgressRepository goalProgressRepository;

    public GroupRankingResponseDTO getGroupRanking(Long groupId, Long loginUserId, String scope) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        MemberEntity loginUser = memberRepository.findById(loginUserId)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + loginUserId));

        // 그룹 멤버 여부 확인
        if (!group.getMembers().contains(loginUser)) {
            throw new RuntimeException("해당 그룹의 멤버만 랭킹을 조회할 수 있습니다.");
        }

        RankingScope rankingScope;
        try {
            rankingScope = RankingScope.valueOf(scope.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("scope must be weekly or total");
        }

        List<Object[]> rawRanks;
        if (rankingScope == RankingScope.WEEKLY) {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
            LocalDateTime startDateTime = startOfWeek.atStartOfDay();
            LocalDateTime endDateTime = startDateTime.plusDays(7);

            rawRanks = goalProgressRepository.findGroupRankingWeekly(groupId, startDateTime, endDateTime);
        } else {
            rawRanks = goalProgressRepository.findGroupRankingTotal(groupId);
        }

        List<GroupRankingItemDTO> items = new ArrayList<>();

        Integer myRank = 0;
        Integer myAchieveCount = 0;
        String myName = loginUser.getNickname();
        String myProfileImage = loginUser.getImageUrl();

        int previousCount = -1;
        int currentRank = 0;

        for (int i = 0; i < rawRanks.size(); i++) {
            Object[] row = rawRanks.get(i);

            Long memberId = (Long) row[0];
            String nickname = (String) row[1];
            String profileImage = (String) row[2];
            Integer achieveCount = ((Long) row[3]).intValue();

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
                scope.toLowerCase(),
                myRank,
                loginUserId,
                myName,
                myProfileImage,
                myAchieveCount,
                items
        );
    }
}