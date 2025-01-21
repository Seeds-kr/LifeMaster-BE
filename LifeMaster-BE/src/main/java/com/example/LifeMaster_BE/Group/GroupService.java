package com.example.LifeMaster_BE.Group;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GoalRepository goalRepository;

    public GroupService(GroupRepository groupRepository, GoalRepository goalRepository) {
        this.groupRepository = groupRepository;
        this.goalRepository = goalRepository;
    }

    // Create a group
    public GroupEntity createGroup(GroupEntity group) {
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
    public GroupEntity updateGroup(Long id, GroupEntity updatedGroup) {
        GroupEntity existingGroup = getGroupById(id);
        existingGroup.setIcon(updatedGroup.getIcon());
        existingGroup.setName(updatedGroup.getName());
        existingGroup.setDescription(updatedGroup.getDescription());
        existingGroup.setStatistics(updatedGroup.getStatistics());
        existingGroup.setPassword(updatedGroup.getPassword());
        return groupRepository.save(existingGroup);
    }

    // Delete a group
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
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
}
