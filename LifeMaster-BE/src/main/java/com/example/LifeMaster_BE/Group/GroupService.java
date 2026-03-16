package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
import com.example.LifeMaster_BE.Group.GoalAchievement.GoalAchievementRepository;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressEntity;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressRepository;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import com.example.LifeMaster_BE.Group.GroupExit.GroupExitHistoryService;
import com.example.LifeMaster_BE.Group.GroupMember.GroupMemberException;
import com.example.LifeMaster_BE.Group.GroupMember.GroupMemberRole;
import com.example.LifeMaster_BE.TimeManager.Sleep.Sleep;
import com.example.LifeMaster_BE.TimeManager.Sleep.SleepRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.example.LifeMaster_BE.Group.GroupMember.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.LifeMaster_BE.Group.Goal.GoalCondition;
import com.example.LifeMaster_BE.Group.Goal.GoalDuration;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GroupService {

    private final SleepRepository sleepRepository;

    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;
    private final GoalProgressRepository goalProgressRepository;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final GroupExitHistoryService groupExitHistoryService;
    private final GoalProgressService goalProgressService;

    private final GroupMemberService groupMemberService;

    private final GoalAchievementRepository goalAchievementRepository;


    // Create a group
    @Transactional
    public GroupEntity createGroup(
            String name,
            String description,
            String icon,
            List<Long> statistics,
            String password,
            GroupAccessType accesstype,
            Long creatorId,
            String creatorEmail
    ) {
        // 0) 기본 검증/기본값
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name is required.");
        }
        if (accesstype == null) {
            accesstype = GroupAccessType.PUBLIC;
        }

        // 1) 생성자 조회
        MemberEntity creator = null;
        if (creatorId != null) creator = memberRepository.findById(creatorId).orElse(null);
        if (creator == null && creatorEmail != null) {
            creator = memberRepository.findByEmail(creatorEmail)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Member not found by id=" + creatorId + " or email=" + creatorEmail));
        }
        if (creator == null) throw new IllegalArgumentException("Creator not resolved (id/email both invalid).");

        // 2) null 처리
        String effectiveIcon = (icon != null) ? icon : "";
        String effectiveDescription = (description != null) ? description : "";

        // 3) 서비스는 '해싱'만 한다 (정책은 엔티티/enum이 강제)
        String encodedPassword = null;
        if (password != null && !password.isBlank()) {
            encodedPassword = passwordEncoder.encode(password);
        }

        GroupEntity group = new GroupEntity(
                effectiveIcon,
                name,
                effectiveDescription,
                statistics,
                encodedPassword,
                creator,
                accesstype
        );

        group.addMember(creator);

        GroupEntity saved = groupRepository.save(group);
        groupMemberService.ensureOwner(saved.getId(), creator.getId());

        return saved;
    }

    // Retrieve all groups
    public List<GroupResponseDto> getAllGroups() {
        return groupRepository.findAllWithMemberCount();
    }

    // Retrieve a group by ID
    public GroupResponseDto getGroupById(Long id) {
        return groupRepository.findByIdWithMemberCount(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found with id " + id));
    }

    // 사용자가 속한 그룹들을 반환하는 메소드
    public List<GroupResponseDto> getGroupsByUser(Long userId) {
        // user 존재 확인만 하고(원하면 생략 가능), 실제 그룹은 count 포함 쿼리로 반환
        memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        return groupRepository.findMyGroupsWithMemberCount(userId);
    }

    // Update a group
    // 선택적인 값만 업데이트하는 메소드
    public GroupEntity updateGroup(
            Long id,
            String name,
            String description,
            String icon,
            List<Long> statistics,
            String password,
            GroupAccessType accessType,
            Long requestUserId
    ) {
        GroupEntity existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + id));

        // OWNER만
        groupMemberService.requireOwner(id, requestUserId);

        // 선택적으로 값을 업데이트
        if (name != null) {
            existingGroup.setName(name);
        }
        if (description != null) {
            existingGroup.setDescription(description);
        }
        if (icon != null) {
            existingGroup.setIcon(icon);
        }
        if (statistics != null) {
            existingGroup.setStatistics(statistics);
        }

        if (accessType != null) {
            existingGroup.setAccessType(accessType);

            // PUBLIC만 비밀번호 제거
            if (accessType == GroupAccessType.PUBLIC) {
                existingGroup.setPassword(null);
            }
        }

        if (password != null) {
            if (!existingGroup.getAccessType().requiresPassword()) {
                throw new IllegalArgumentException("This group type cannot set password.");
            }

            if (password.isBlank()) {
                existingGroup.setPassword(null);
            } else {
                existingGroup.setPassword(passwordEncoder.encode(password));
            }
        }

        return groupRepository.save(existingGroup);
    }

    @Transactional
    public void deleteGroup(Long id, Long requestUserId, String password) {

        groupMemberService.requireOwner(id, requestUserId);

        GroupEntity group = groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + id));

        // 비밀번호 검증
        String stored = group.getPassword();
        boolean hasPassword = stored != null && !stored.isBlank();
        if (hasPassword) {
            if (password == null || password.isBlank()) {
                throw new IllegalArgumentException("Password is required to delete this group.");
            }
            if (!passwordEncoder.matches(password, stored)) {
                throw new IllegalArgumentException("Invalid group password.");
            }
        }

        // 1) 권한/멤버십(별도 테이블) 먼저 삭제
        groupMemberService.deleteAllByGroupId(id);

        // 2) 탈퇴/히스토리 등 groupId FK 가진 것들 삭제
        groupExitHistoryService.deleteByGroupId(id);

        // 3) 목표 달성 기록 삭제
        goalAchievementRepository.deleteByGroupId(id);

        // 4) 목표 진행도 삭제
        goalProgressService.deleteByGroupId(id);

        // 5) 목표 삭제
        goalRepository.deleteByGroupId(id);

        // 6) ManyToMany 조인 정리
        for (MemberEntity m : new HashSet<>(group.getMembers())) {
            m.getGroups().remove(group);
        }
        group.getMembers().clear();

        // 7) 마지막에 그룹 삭제
        groupRepository.delete(group);
    }

    // 목표를 그룹에 추가
    public GroupEntity addGoalToGroup(Long groupId, GoalEntity goal) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        // enum/null 방어
        if (goal.getDuration() == null) {
            throw new RuntimeException("Goal duration is required.");
        }

        if (goal.getGoalCondition() == null) {
            throw new RuntimeException("Goal condition is required.");
        }

        // 값 방어
        if (goal.getValue() <= 0) {
            throw new RuntimeException("Goal value must be greater than 0.");
        }

        // 이미 목표가 그룹에 존재하는지 체크
        boolean goalExists = group.getGoals().stream()
                .anyMatch(existingGoal -> existingGoal.getName().equals(goal.getName()));

        if (goalExists) {
            throw new RuntimeException("Goal already exists in the group.");
        }

        goal.setGroup(group);
        group.addGoal(goal);

        groupRepository.save(group);
        return group;
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

    @Transactional
    public void deleteGoal(Long groupId, Long goalId) {
        GoalEntity goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + goalId));

        if (!goal.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("Goal does not belong to the specified group.");
        }

        // 1) 목표 달성 기록 삭제
        goalAchievementRepository.deleteByGoalId(goalId);

        // 2) 목표 진행 기록 삭제
        goalProgressRepository.deleteByGoalId(goalId);

        // 3) 목표 삭제
        goalRepository.delete(goal);
    }

    @Transactional
    public String addUserToGroup(Long groupId, Long userId) {
        // 과거 방식(요청자=대상자)로 취급 → "자기 자신이 가입" 같은 용도
        return addUserToGroup(groupId, userId, userId);
    }

    @Transactional
    public String addUserToGroup(Long groupId, Long requestUserId, Long targetUserId) {

        // 권한 설정
        groupMemberService.requireAtLeastAdmin(groupId, requestUserId);

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        MemberEntity member = memberRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + targetUserId));

        if (group.getMembers().contains(member)) {
            return "User already in the group.";
        }

        // 1) ManyToMany 추가
        group.getMembers().add(member);
        member.getGroups().add(group);
        groupRepository.save(group);

        // 2) 권한 엔티티 추가 (여긴 이제 안전)
        groupMemberService.addMember(groupId, requestUserId, targetUserId, null);

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
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        List<GoalEntity> goals = group.getGoals();

        List<MemberEntity> allUsers = group.getMembers().stream()
                .sorted(Comparator.comparing(MemberEntity::getEmail))
                .toList();

        List<Map<String, Object>> goalProgressList = new ArrayList<>();

        for (GoalEntity goal : goals) {
            LocalDateTime startTime = getStartDateTimeForCurrentPeriod(goal.getDuration());

            List<GoalProgressEntity> progressList =
                    goalProgressRepository.findByGoalAndSubmittedAtAfter(goal, startTime);

            Map<String, Object> goalData = new HashMap<>();
            goalData.put("goalId", goal.getId());
            goalData.put("goalName", goal.getName());
            goalData.put("goalCreationTime", goal.getCreatedAt());
            goalData.put("goalDuration", goal.getDuration());
            goalData.put("goalValue", goal.getValue());
            goalData.put("goalCondition", goal.getGoalCondition());

            List<Map<String, Object>> userProgressList = new ArrayList<>();

            int goalValue = goal.getValue();

            for (MemberEntity user : allUsers) {
                int totalProgress = progressList.stream()
                        .filter(progress -> progress.getUser().getId().equals(user.getId()))
                        .mapToInt(GoalProgressEntity::getProgressValue)
                        .sum();

                double progressPercentage;
                boolean isAchieved;

                if (goalValue <= 0) {
                    progressPercentage = 0.0;
                    isAchieved = false;
                } else {
                    double raw = (totalProgress / (double) goalValue) * 100.0;
                    progressPercentage = Math.min(raw, 100.0);
                    isAchieved = totalProgress >= goalValue;
                }

                Map<String, Object> userProgressData = new HashMap<>();
                userProgressData.put("userId", user.getId());
                userProgressData.put("userEmail", user.getEmail());
                userProgressData.put("progressPercentage", String.format("%.1f%%", progressPercentage));
                userProgressData.put("progressValue", totalProgress);
                userProgressData.put("isAchieved", isAchieved);

                userProgressList.add(userProgressData);
            }

            goalData.put("userProgress", userProgressList);
            goalProgressList.add(goalData);
        }

        return goalProgressList;
    }

    private LocalDateTime getStartDateTimeForCurrentPeriod(GoalDuration duration) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return switch (duration) {
            case DAILY -> today.atStartOfDay();
            case WEEKLY -> today.with(DayOfWeek.MONDAY).atStartOfDay();
            case MONTHLY -> today.withDayOfMonth(1).atStartOfDay();
            default -> throw new IllegalArgumentException("Invalid duration: " + duration);
        };
    }

    // 새 메서드 추가 (요청자 기반 권한처리 가능)
    @Transactional
    public void kickMember(Long groupId, Long requestUserId, Long targetUserId) {

        // OWNER만 강퇴 가능
        groupMemberService.requireOwner(groupId, requestUserId);

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        MemberEntity target = memberRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + targetUserId));

        if (!group.getMembers().contains(target)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        // OWNER는 강퇴 불가
        GroupMemberRole targetRole = groupMemberService.getRole(groupId, targetUserId);
        if (targetRole == GroupMemberRole.OWNER) {
            throw new GroupMemberException("OWNER cannot be removed.");
        }

        // 권한 엔티티 제거
        groupMemberService.removeMember(groupId, requestUserId, targetUserId);

        // 탈퇴 기록 저장
        groupExitHistoryService.recordGroupExit(groupId, targetUserId);

        // ManyToMany 제거
        group.getMembers().remove(target);
        target.getGroups().remove(group);

        groupRepository.save(group);
    }

    // 본인 탈퇴 전용: MEMBER/ADMIN 가능, OWNER 불가
    @Transactional
    public void leaveGroup(Long groupId, Long requestUserId) {

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        MemberEntity member = memberRepository.findById(requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + requestUserId));

        if (!group.getMembers().contains(member)) {
            throw new IllegalArgumentException("User is not a member of this group.");
        }

        // OWNER는 탈퇴 불가
        GroupMemberRole myRole = groupMemberService.getRole(groupId, requestUserId);
        if (myRole == GroupMemberRole.OWNER) {
            throw new GroupMemberException("OWNER cannot leave. Transfer ownership first.");
        }

        // 권한 엔티티 제거 (self 탈퇴)
        groupMemberService.removeMember(groupId, requestUserId, requestUserId);

        // 탈퇴 기록 저장
        groupExitHistoryService.recordGroupExit(groupId, requestUserId);

        // ManyToMany 제거
        group.getMembers().remove(member);
        member.getGroups().remove(group);

        // 마지막 멤버였으면 그룹 자체 정리 삭제
        if (group.getMembers().isEmpty()) {

            // 목표 달성 기록 삭제
            goalAchievementRepository.deleteByGroupId(groupId);
            // 목표 진행도 삭제
            goalProgressService.deleteByGroupId(groupId);
            // 목표 삭제
            goalRepository.deleteByGroupId(groupId);
            // 탈퇴 기록 삭제
            groupExitHistoryService.deleteByGroupId(groupId);
            // 권한 엔티티 삭제
            groupMemberService.deleteAllByGroupId(groupId);
            // 그룹 삭제
            groupRepository.delete(group);

        } else {
            groupRepository.save(group);
        }
    }

    public void deleteByGroupId(Long groupId) {
        goalRepository.deleteByGroupId(groupId);
    }

    // 초대 코드 생성 (그룹 ID + 해싱된 비밀번호 조합)
    public String generateInviteCode(Long groupId, Long requestUserId) {

        groupMemberService.requireAtLeastAdmin(groupId, requestUserId);

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (group.getAccessType() == GroupAccessType.PUBLIC) {
            throw new IllegalArgumentException("Public group does not require invite code.");
        }

        String groupPasswordHash = group.getPassword();
        if (groupPasswordHash == null || groupPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Group password is not set.");
        }

        return groupId + ":" + groupPasswordHash;
    }

    // 초대 코드로 그룹 가입
    @Transactional
    public String joinGroupWithInviteCode(Long userId, String inviteCode) {

        String[] parts = inviteCode.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid invite code format. Expected: groupId:passwordHash");
        }

        Long groupId = Long.parseLong(parts[0]);
        String inviteHash = parts[1];

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        String dbHash = group.getPassword();
        if (dbHash == null || dbHash.isBlank()) {
            throw new IllegalArgumentException("Group password is not set.");
        }

        // ✅ 해시 문자열 동일 비교
        if (!dbHash.equals(inviteHash)) {
            throw new IllegalArgumentException("Invalid invite code.");
        }

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (group.getMembers().contains(member)) {
            return "User already in the group.";
        }

        group.getMembers().add(member);
        member.getGroups().add(group);
        groupRepository.save(group);

        groupMemberService.joinAsMember(groupId, userId);

        return "User successfully joined the group.";
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void calculateAverageSleepTimeForAllGroups() {
        List<GroupEntity> groups = groupRepository.findAll();
        for (GroupEntity group : groups) {
            calculateAverageSleepTime(group);
        }
    }

    // 평균 수면 시간 계산 메서드
    public void calculateAverageSleepTime(GroupEntity group) {
        long totalSleepTime = 0;
        int userCount = 0;
        LocalDateTime averTime;

        Set<MemberEntity> users = group.getMembers();
        List<MemberEntity> userList = users.stream()
                .collect(Collectors.toList());
        List<Sleep> sleeps = sleepRepository.findAllByUser(userList);

        /* 각 유저의 수면 기록을 가져와서 시간 계산
        for (MemberEntity user : users) {
            Sleep sleep = sleepRepository.findByUser(user);
            for (Sleep record : sleeps) {
                totalSleepTime += record.getSleepDuration().toMinutes(); // 수면 시간 합산
                userCount++;
            }
        }*/
        for (Sleep record : sleeps) {
            totalSleepTime += record.getSleepDuration().toMinutes();
            userCount++;
        }

        if (userCount > 0) {
            long averageSleepTimeInMinutes = totalSleepTime / userCount; // 평균 수면 시간 계산
            averTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(averageSleepTimeInMinutes * 60), ZoneOffset.UTC);
            GroupStatic groupStatic = GroupStatic.builder()
                    .group(group)
                    .date(new Date())
                    .averTime(averTime)
                    .build();

        } else {
            averTime = null;
        }
    }

    public GroupDto.Static getUserStatic(String email, GroupEntity group) {
        MemberEntity user = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        List<Long> userSleepDurations = new ArrayList<>();
        List<Long> groupAverageSleepDurations = new ArrayList<>();

        LocalDate today = LocalDate.now();

        // 최근 7일 데이터 조회
        for (int i = 6; i >= 0; i--) {
            LocalDate targetDate = today.minusDays(i);

            // 유저 수면 기록 조회
            List<Sleep> userSleeps = sleepRepository.findByUserAndDate(user, targetDate);
            long userTotalSleep = userSleeps.stream()
                    .mapToLong(sleep -> sleep.getSleepDuration().toMinutes())
                    .sum();
            userSleepDurations.add(userTotalSleep);

            // 그룹 멤버들의 수면 기록 조회
            Set<MemberEntity> members = group.getMembers();
            List<MemberEntity> memberss = members.stream()
                    .collect(Collectors.toList());
            ;
            List<Sleep> groupSleeps = sleepRepository.findAllByUserAndDate(memberss, targetDate);

            long groupTotalSleep = groupSleeps.stream()
                    .mapToLong(sleep -> sleep.getSleepDuration().toMinutes())
                    .sum();

            long groupAverageSleep = members.isEmpty() ? 0 : groupTotalSleep / members.size();
            groupAverageSleepDurations.add(groupAverageSleep);
        }

        return GroupDto.Static.builder()
                .userSleepDurations(userSleepDurations)
                .groupAverageSleepDurations(groupAverageSleepDurations)
                .build();
    }
    //유저 id로 목표 조회
    public List<GoalEntity> getGoalsByMemberId (Long memberId){
        return groupRepository.findGoalsByMemberId(memberId);
    }

    //그룹 비밀번호 재설정
    @Transactional
    public void resetGroupPassword(Long groupId, Long requestUserId, String ownerPassword, String newGroupPassword) {

        groupMemberService.requireOwner(groupId, requestUserId);

        MemberEntity owner = memberRepository.findById(requestUserId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found."));

        if (ownerPassword == null || ownerPassword.isBlank()) {
            throw new IllegalArgumentException("Owner password is required.");
        }
        if (!passwordEncoder.matches(ownerPassword, owner.getPassword())) {
            throw new IllegalArgumentException("Invalid owner password.");
        }

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        // PUBLIC이면 비번 기능 자체가 없음
        if (!group.getAccessType().requiresPassword()) {
            throw new IllegalArgumentException("This group type cannot have password.");
        }

        if (newGroupPassword == null || newGroupPassword.isBlank()) {
            group.setPassword(null); // 엔티티 validate에서 PRIVATE/PASSWORD면 여기서 예외가 날 거야(=비번 제거 불가 정책)
        } else {
            group.setPassword(passwordEncoder.encode(newGroupPassword));
        }

        groupRepository.save(group);
    }

    @Transactional
    public String joinGroup(Long groupId, Long userId, String password) {

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 이미 가입 여부
        if (group.getMembers().contains(member)) {
            return "User already in the group.";
        }

        // 그룹 접근 타입 확인
        switch (group.getAccessType()) {

            case PUBLIC:
                // 바로 가입 가능
                break;

            case PASSWORD:
                if (password == null || password.isBlank()) {
                    throw new IllegalArgumentException("Password is required to join this group.");
                }

                if (!passwordEncoder.matches(password, group.getPassword())) {
                    throw new IllegalArgumentException("Invalid group password.");
                }
                break;

            case PRIVATE:
                throw new IllegalArgumentException("This group is private. Invite code required.");
        }

        // 그룹 가입
        group.getMembers().add(member);
        member.getGroups().add(group);

        groupRepository.save(group);

        // 권한 엔티티 추가
        groupMemberService.joinAsMember(groupId, userId);

        return "Successfully joined the group.";
    }

    @Transactional
    public void clearGroupPassword(Long groupId, Long requestUserId, String ownerPassword) {
        resetGroupPassword(groupId, requestUserId, ownerPassword, null);
    }
}
