package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressEntity;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.Group.GroupExit.GroupExitHistoryService;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final GroupExitHistoryService groupExitHistoryService;
    private final GoalProgressService goalProgressService;

    public GroupService(GroupRepository groupRepository, GoalRepository goalRepository, GoalProgressRepository goalProgressRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder, GroupExitHistoryService groupExitHistoryService, GoalProgressService goalProgressService) {
        this.groupRepository = groupRepository;
        this.goalRepository = goalRepository;
        this.goalProgressRepository = goalProgressRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupExitHistoryService = groupExitHistoryService;
        this.goalProgressService = goalProgressService;
    }

    // Create a group
    @Transactional
    public GroupEntity createGroup(String name, String description, String icon, List<Long> statistics, String password, Long creatorId) {
        // 그룹 생성자를 가져오기
        MemberEntity creator = memberRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + creatorId));

        // null 처리: 아이콘과 비밀번호에 기본값 적용
        String effectiveIcon = (icon != null) ? icon : ""; // 기본 아이콘 설정
        String effectivePassword = (password != null) ? password : ""; // 기본 비밀번호 설정
        String effectiveDescription = (password != null) ? password : "";

        // 그룹 생성
        GroupEntity group = new GroupEntity(effectiveIcon, name, effectiveDescription, statistics, effectivePassword, creator);

        // 생성자를 그룹 멤버로 자동 추가
        group.getMembers().add(creator);
        creator.getGroups().add(group);

        return groupRepository.save(group);
    }

    // Retrieve all groups
    public List<GroupEntity> getAllGroups() {
        return groupRepository.findAll();
    }

    // Retrieve a group by ID
    public GroupEntity getGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id " + id));
    }

    // Update a group
    // 선택적인 값만 업데이트하는 메소드
    public GroupEntity updateGroup(Long id, String name, String description, String icon, List<Long> statistics, String password) {
        GroupEntity existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + id));

        // 선택적으로 값을 업데이트
        if (name != null) {
            existingGroup.setName(name);
        }
        if (description != null) {
            existingGroup.setDescription(description);
        }
        if (icon != null) {
            existingGroup.setPassword(icon);
        }
        if (statistics != null) {
            existingGroup.setStatistics(statistics);
        }
        if (password != null) {
            existingGroup.setIcon(password);
        }

        return groupRepository.save(existingGroup);
    }

    @Transactional
    public void deleteGroup(Long id) {
        // 그룹 조회
        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + id));

        // 그룹의 creator를 가져옴
        MemberEntity creator = group.getCreator();

        // creator가 그룹의 멤버에서 제거되도록 처리
        if (creator != null) {
            creator.getGroups().remove(group); // creator가 속한 그룹 목록에서 그룹 제거
        }

        // 그룹의 멤버 목록에서 creator를 제거
        group.getMembers().remove(creator);

        // 그룹의 목표 진행 상황 (GoalProgressEntity) 삭제
        List<GoalProgressEntity> goalProgressList = goalProgressRepository.findByGroup(group);
        goalProgressRepository.deleteAll(goalProgressList); // 해당 그룹의 모든 진행 상황 삭제


        // 그룹을 삭제
        groupRepository.delete(group);
    }

    // 목표를 그룹에 추가
    public GroupEntity addGoalToGroup(Long groupId, GoalEntity goal) {
        // GroupEntity 조회
        Optional<GroupEntity> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isPresent()) {
            GroupEntity group = groupOptional.get();

            // duration이 유효한 값인지 확인 (daily, weekly, monthly)
            if (!isValidDuration(goal.getDuration())) {
                throw new RuntimeException("Invalid duration value. Must be daily, weekly, or monthly.");
            }

            // goalCondition이 유효한 값인지 확인 (time, count)
            if (!isValidGoalCondition(goal.getGoal_condition())) {
                throw new RuntimeException("Invalid goal condition. Must be time or count.");
            }

            // 이미 목표가 그룹에 존재하는지 체크 (중복 추가 방지)
            boolean goalExists = group.getGoals().stream()
                    .anyMatch(existingGoal -> existingGoal.getName().equals(goal.getName()));

            if (goalExists) {
                throw new RuntimeException("Goal already exists in the group.");
            }

            // GoalEntity의 group 설정
            goal.setGroup(group);

            // 목표를 그룹에 추가
            group.addGoal(goal); // 그룹에 목표 추가

            // 그룹을 저장하여 목표도 함께 저장
            groupRepository.save(group); // 이 호출만으로 목표도 저장됨

            return group;
        } else {
            throw new RuntimeException("Group not found with id: " + groupId);
        }
    }

    // duration이 유효한 값인지 확인하는 메소드
    private boolean isValidDuration(String duration) {
        return "daily".equalsIgnoreCase(duration) || "weekly".equalsIgnoreCase(duration) || "monthly".equalsIgnoreCase(duration);
    }

    // goalCondition이 유효한 값인지 확인하는 메소드
    private boolean isValidGoalCondition(String goalCondition) {
        return "time".equalsIgnoreCase(goalCondition) || "count".equalsIgnoreCase(goalCondition);
    }


    public GroupEntity findById(Long groupId) {
        // groupRepository에서 그룹을 찾아 옵니다.
        Optional<GroupEntity> groupOptional = groupRepository.findById(groupId);

        // 그룹이 존재하면 해당 그룹을 반환하고, 그렇지 않으면 예외를 던집니다.
        if (groupOptional.isPresent()) {
            return groupOptional.get();
        } else {
            throw new RuntimeException("Group not found with id: " + groupId);
        }
    }

    public void deleteGoal(Long groupId, Long goalId) {
        // 목표 찾기
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));

        // 목표가 속한 그룹이 올바른지 확인
        if (!goal.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Goal does not belong to the specified group.");
        }

        // 목표와 관련된 진행 상황 (GoalProgressEntity) 삭제
        List<GoalProgressEntity> goalProgressList = goalProgressRepository.findByGoal(goal);
        goalProgressRepository.deleteAll(goalProgressList); // 해당 목표의 모든 진행 상황 삭제

        // 목표 삭제
        goalRepository.delete(goal);
    }

    @Transactional
    public String addUserToGroup(Long groupId, Long userId) {
        // 그룹 조회
        Optional<GroupEntity> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            throw new IllegalArgumentException("Group not found with ID: " + groupId);
        }

        // 사용자 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(userId);
        if (memberOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }

        // 그룹과 사용자 연결
        GroupEntity group = groupOpt.get();
        MemberEntity member = memberOpt.get();

        group.getMembers().add(member);
        member.getGroups().add(group);

        groupRepository.save(group); // 그룹 저장
        return "User added to group successfully";
    }

    // 목표 ID를 그룹의 통계에 추가하는 메소드
    @Transactional
    public GroupEntity addStatisticGoal(Long groupId, Long goalId) {
        // 그룹과 목표를 조회
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new IllegalArgumentException("Goal not found with ID: " + goalId));

        // 그룹의 통계에 목표 ID를 추가
        if (!group.getStatistics().contains(goalId)) {
            group.getStatistics().add(goalId);
        }

        // 변경된 그룹 저장
        return groupRepository.save(group);
    }

    // 사용자가 속한 그룹들을 반환하는 메소드
    public List<GroupEntity> getGroupsByUser(Long userId) {
        // 사용자를 조회
        MemberEntity user = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 사용자가 속한 그룹들을 반환
        return new ArrayList<>(user.getGroups());
    }

    // 그룹에 속한 사용자들을 반환하는 메소드
    public List<MemberEntity> getUsersByGroup(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        return new ArrayList<>(group.getMembers());
    }

    // 그룹에서 통계 항목 삭제하는 메소드
    public GroupEntity removeStatisticFromGroup(Long groupId, Long statistic) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        // 통계 항목이 존재하는지 확인
        if (!group.getStatistics().contains(statistic)) {
            throw new IllegalArgumentException("Statistic not found in the group.");
        }

        // 통계 항목 삭제
        group.getStatistics().remove(statistic);

        // 그룹 저장
        return groupRepository.save(group);
    }

    public List<Map<String, Object>> getGroupGoalProgress(Long groupId) {
        Optional<GroupEntity> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isEmpty()) {
            throw new RuntimeException("Group not found with id: " + groupId);
        }

        GroupEntity group = groupOptional.get();
        List<GoalEntity> goals = group.getGoals();

        List<String> allUsers = group.getMembers() // group.getMembers()로 변경
                .stream()
                .map(MemberEntity::getEmail) // MemberEntity에서 이메일 추출
                .sorted()
                .toList();

        List<Map<String, Object>> goalProgressList = new ArrayList<>();

        for (GoalEntity goal : goals) {
            List<GoalProgressEntity> progressList = goalProgressRepository.findByGoal(goal);

            Map<String, Object> goalData = new HashMap<>();
            goalData.put("goalName", goal.getName());
            goalData.put("goalCreationTime", goal.getCreatedAt());
            goalData.put("goalDuration", goal.getDuration());
            goalData.put("goalValue", goal.getValue());
            goalData.put("goalCondition", goal.getGoal_condition());

            // 유저별 진행 정보 계산
            List<Map<String, Object>> userProgressList = new ArrayList<>();

            for (String userEmail : allUsers) { // 🔹 그룹 유저 전원 포함
                List<GoalProgressEntity> userProgresses = progressList.stream()
                        .filter(progress -> progress.getUserEmail().equals(userEmail))
                        .toList();

                int totalProgress = userProgresses.stream()
                        .mapToInt(GoalProgressEntity::getProgressValue)
                        .sum();

                double progressPercentage = (totalProgress / (double) goal.getValue()) * 100;

                Map<String, Object> userProgressData = new HashMap<>();
                userProgressData.put("userEmail", userEmail);
                userProgressData.put("progressPercentage", String.format("%.1f%%", progressPercentage));
                userProgressData.put("progressValue", totalProgress);

                userProgressList.add(userProgressData);
            }

            goalData.put("userProgress", userProgressList);
            goalProgressList.add(goalData);
        }

        return goalProgressList;
    }

    @Transactional
    public void removeUserFromGroup(Long groupId, Long memberId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + memberId));

        if (!group.getMembers().contains(member)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        // 탈퇴 기록 저장
        groupExitHistoryService.recordGroupExit(groupId, memberId);

        // 그룹에서 멤버 제거
        group.getMembers().remove(member);
        member.getGroups().remove(group);

        if (group.getMembers().isEmpty()) {
            goalProgressService.deleteByGroupId(groupId);
            deleteByGroupId(groupId);
            groupExitHistoryService.deleteByGroupId(groupId);
            // 그룹에 남아있는 멤버가 없으면 그룹 삭제
            groupRepository.delete(group);
        } else {
            // 멤버가 남아 있으면 변경사항 저장
            groupRepository.save(group);
        }
    }

    public void deleteByGroupId(Long groupId) {
        goalRepository.deleteByGroupId(groupId);
    }

    // ✅ 초대 코드 생성 (그룹 ID + 해싱된 비밀번호 조합)
    public String generateInviteCode(Long groupId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        return groupId + "-" + passwordEncoder.encode(group.getPassword());
    }

    // ✅ 초대 코드로 그룹 가입
    @Transactional
    public String joinGroupWithInviteCode(Long userId, String inviteCode) {
        String[] parts = inviteCode.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid invite code format.");
        }

        Long groupId = Long.parseLong(parts[0]);
        String hashedPassword = parts[1];

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (!passwordEncoder.matches(group.getPassword(), hashedPassword)) {
            throw new IllegalArgumentException("Invalid invite code.");
        }

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 사용자를 그룹에 추가
        group.getMembers().add(member);
        member.getGroups().add(group);

        groupRepository.save(group);
        return "User successfully joined the group.";
    }

    //유저 id로 목표 조회
    public List<GoalEntity> getGoalsByMemberId(Long memberId) {
        return groupRepository.findGoalsByMemberId(memberId);
    }
}
