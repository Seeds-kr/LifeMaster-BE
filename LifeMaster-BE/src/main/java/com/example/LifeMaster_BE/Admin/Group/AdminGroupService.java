package com.example.LifeMaster_BE.Admin.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalAchievementRepository;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupExit.GroupExitHistoryService;
import com.example.LifeMaster_BE.Group.GroupMember.GroupMemberService;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminGroupService {

    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;
    private final GoalAchievementRepository goalAchievementRepository;
    private final GoalProgressService goalProgressService;
    private final GroupExitHistoryService groupExitHistoryService;
    private final GroupMemberService groupMemberService;

    public AdminGroupDto.GroupDashboard getGroupDashboard() {

        List<GroupEntity> groupEntities = groupRepository.findAll();

        List<AdminGroupDto.GroupListItem> groups = groupEntities.stream()
                .sorted(Comparator.comparing(GroupEntity::getId).reversed())
                .map(this::toGroupListItem)
                .toList();

        long totalGroups = groups.size();
        long emptyGroups = groups.stream()
                .filter(group -> group.getMemberCount() == 0)
                .count();

        long privateGroups = groups.stream()
                .filter(group -> "PRIVATE".equalsIgnoreCase(group.getAccessType()))
                .count();

        long abnormalGroups = groups.stream()
                .filter(AdminGroupDto.GroupListItem::isAbnormal)
                .count();

        AdminGroupDto.Summary summary = AdminGroupDto.Summary.builder()
                .totalGroups(totalGroups)
                .emptyGroups(emptyGroups)
                .privateGroups(privateGroups)
                .abnormalGroups(abnormalGroups)
                .build();

        return AdminGroupDto.GroupDashboard.builder()
                .summary(summary)
                .groups(groups)
                .build();
    }

    public List<AdminGroupDto.GroupListItem> getAllGroups() {
        return groupRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GroupEntity::getId).reversed())
                .map(this::toGroupListItem)
                .toList();
    }

    public List<AdminGroupDto.GroupMemberItem> getGroupMembers(Long groupId) {

        GroupEntity group = getGroupOrThrow(groupId);

        return group.getMembers()
                .stream()
                .sorted(Comparator.comparing(MemberEntity::getId))
                .map(member -> AdminGroupDto.GroupMemberItem.builder()
                        .memberId(member.getId())
                        .email(member.getEmail())
                        .nickname(member.getNickname())
                        .build())
                .toList();
    }

    public AdminGroupDto.GroupActivity getGroupActivity(Long groupId) {

        GroupEntity group = getGroupOrThrow(groupId);

        AbnormalResult abnormalResult = checkAbnormal(group);

        int statisticGoalCount = group.getStatistics() == null
                ? 0
                : group.getStatistics().size();

        return AdminGroupDto.GroupActivity.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .memberCount(group.getMembers() == null ? 0 : group.getMembers().size())
                .goalCount(group.getGoals() == null ? 0 : group.getGoals().size())
                .statisticGoalCount(statisticGoalCount)
                .abnormal(abnormalResult.abnormal())
                .abnormalReason(abnormalResult.reason())
                .build();
    }

    @Transactional
    public void deleteAbnormalGroup(Long groupId) {

        GroupEntity group = getGroupOrThrow(groupId);

        AbnormalResult abnormalResult = checkAbnormal(group);

        if (!abnormalResult.abnormal()) {
            throw new IllegalArgumentException("비정상 그룹으로 판단되지 않아 관리자 삭제를 중단합니다.");
        }

        forceDeleteGroup(group);
    }

    @Transactional
    public void forceDeleteGroup(Long groupId) {
        GroupEntity group = getGroupOrThrow(groupId);
        forceDeleteGroup(group);
    }

    private void forceDeleteGroup(GroupEntity group) {

        Long groupId = group.getId();

        // 기존 GroupService.deleteGroup()의 삭제 순서와 동일하게 정리
        groupMemberService.deleteAllByGroupId(groupId);
        groupExitHistoryService.deleteByGroupId(groupId);
        goalAchievementRepository.deleteByGroupId(groupId);
        goalProgressService.deleteByGroupId(groupId);
        goalRepository.deleteByGroupId(groupId);

        for (MemberEntity member : new HashSet<>(group.getMembers())) {
            member.getGroups().remove(group);
        }

        group.getMembers().clear();

        groupRepository.delete(group);
    }

    private AdminGroupDto.GroupListItem toGroupListItem(GroupEntity group) {

        AbnormalResult abnormalResult = checkAbnormal(group);

        MemberEntity owner = group.getCreator();

        return AdminGroupDto.GroupListItem.builder()
                .groupId(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .accessType(group.getAccessType() == null ? null : group.getAccessType().name())
                .ownerId(owner == null ? null : owner.getId())
                .ownerEmail(owner == null ? null : owner.getEmail())
                .memberCount(group.getMembers() == null ? 0 : group.getMembers().size())
                .goalCount(group.getGoals() == null ? 0 : group.getGoals().size())
                .abnormal(abnormalResult.abnormal())
                .abnormalReason(abnormalResult.reason())
                .build();
    }

    private AbnormalResult checkAbnormal(GroupEntity group) {

        if (group.getCreator() == null) {
            return new AbnormalResult(true, "생성자 정보 없음");
        }

        int memberCount = group.getMembers() == null ? 0 : group.getMembers().size();

        if (memberCount == 0) {
            return new AbnormalResult(true, "그룹 인원 0명");
        }

        if (!group.getMembers().contains(group.getCreator())) {
            return new AbnormalResult(true, "생성자가 그룹 멤버 목록에 없음");
        }

        if (group.getName() == null || group.getName().isBlank()) {
            return new AbnormalResult(true, "그룹 이름 없음");
        }

        return new AbnormalResult(false, "");
    }

    private GroupEntity getGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
    }

    private record AbnormalResult(boolean abnormal, String reason) {
    }
}