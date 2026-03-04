package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.Goal.GoalRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

            // PUBLIC/PRIVATE로 바꾸면 비번 제거
            if (accessType == GroupAccessType.PUBLIC || accessType == GroupAccessType.PRIVATE) {
                existingGroup.setPassword(null);
            }
        }

        // 비밀번호 변경은 PASSWORD 타입에서만 허용
        if (password != null) {
            if (existingGroup.getAccessType() != GroupAccessType.PASSWORD) {
                throw new IllegalArgumentException("Only PASSWORD group can set password.");
            }
            if (password.isBlank()) {
                existingGroup.setPassword(null); // 비번 제거
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

        // 비밀번호 검증(기존)
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

        // 3) 목표 진행도/목표 등 groupId FK 가진 것들 삭제
        goalProgressService.deleteByGroupId(id);
        goalRepository.deleteByGroupId(id);

        // 4) ManyToMany 조인 정리 (member_group)
        for (MemberEntity m : new HashSet<>(group.getMembers())) {
            m.getGroups().remove(group);
        }
        group.getMembers().clear();

        // 5) 마지막에 그룹 삭제
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

        // ✅ OWNER는 탈퇴 불가
        GroupMemberRole myRole = groupMemberService.getRole(groupId, requestUserId);
        if (myRole == GroupMemberRole.OWNER) {
            throw new GroupMemberException("OWNER cannot leave. Transfer ownership first.");
        }

        // ✅ 권한 엔티티 제거 (self 탈퇴)
        groupMemberService.removeMember(groupId, requestUserId, requestUserId);

        // 탈퇴 기록 저장
        groupExitHistoryService.recordGroupExit(groupId, requestUserId);

        // ManyToMany 제거
        group.getMembers().remove(member);
        member.getGroups().remove(group);

        if (group.getMembers().isEmpty()) {
            goalProgressService.deleteByGroupId(groupId);
            deleteByGroupId(groupId);
            groupExitHistoryService.deleteByGroupId(groupId);

            groupMemberService.deleteAllByGroupId(groupId);

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

        // PUBLIC 그룹은 초대코드 필요 없음
        if (group.getAccessType() == GroupAccessType.PUBLIC) {
            throw new IllegalArgumentException("Public group does not require invite code.");
        }

        String groupPasswordHash = group.getPassword();

        // PRIVATE는 password 없어도 가능
        if (group.getAccessType() == GroupAccessType.PASSWORD) {
            if (groupPasswordHash == null || groupPasswordHash.isBlank()) {
                throw new IllegalArgumentException("Group password is not set.");
            }
        }

        return groupId + ":" + (groupPasswordHash != null ? groupPasswordHash : "private");
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

        // 각 유저의 수면 기록을 가져와서 시간 계산
        for (MemberEntity user : users) {
            Sleep sleep = sleepRepository.findByUser(user);
            for (Sleep record : sleeps) {
                totalSleepTime += record.getSleepDuration().toMinutes(); // 수면 시간 합산
                userCount++;
            }
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
