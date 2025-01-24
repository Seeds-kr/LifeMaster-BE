package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;

    private final MemberRepository memberRepository;

    public GroupService(GroupRepository groupRepository, GoalRepository goalRepository, MemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.goalRepository = goalRepository;
        this.memberRepository = memberRepository;
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

        // 그룹을 삭제
        groupRepository.delete(group);
    }

    // 목표를 그룹에 추가
    public GroupEntity addGoalToGroup(Long groupId, GoalEntity goal) {
        // GroupEntity 조회
        Optional<GroupEntity> groupOptional = groupRepository.findById(groupId);
        if (groupOptional.isPresent()) {
            GroupEntity group = groupOptional.get();

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
}
