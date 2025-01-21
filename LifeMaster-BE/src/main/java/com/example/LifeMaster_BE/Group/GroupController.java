package com.example.LifeMaster_BE.Group;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // Create a new group
    @PostMapping
    public ResponseEntity<GroupEntity> createGroup(@RequestBody GroupEntity group) {
        GroupEntity createdGroup = groupService.createGroup(group);
        return ResponseEntity.ok(createdGroup);
    }

    // Get all groups
    @GetMapping
    public ResponseEntity<List<GroupEntity>> getAllGroups() {
        List<GroupEntity> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    // Get a group by ID
    @GetMapping("/{id}")
    public ResponseEntity<GroupEntity> getGroupById(@PathVariable("id") Long id) {
        GroupEntity group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    // Update a group by ID
    @PutMapping("/{id}")
    public ResponseEntity<GroupEntity> updateGroup(@PathVariable("id") Long id, @RequestBody GroupEntity updatedGroup) {
        GroupEntity group = groupService.updateGroup(id, updatedGroup);
        return ResponseEntity.ok(group);
    }

    // Delete a group by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    // Add a goal to a group
    @PostMapping("/{groupId}/goal")
    public ResponseEntity<GroupEntity> addGoalToGroup(@PathVariable("groupId") Long groupId, @RequestBody GoalDTO goalDTO) {
        GroupEntity group = groupService.findById(groupId);  // GroupEntity 조회
        GoalEntity goal = new GoalEntity();

        goal.setName(goalDTO.getName());
        goal.setGoalCondition(goalDTO.getGoalCondition());
        goal.setValue(goalDTO.getValue());
        goal.setGroup(group);  // GroupEntity 연결

        GroupEntity updatedGroup = groupService.addGoalToGroup(groupId, goal);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}/goal/{goalId}")
    public ResponseEntity<String> deleteGoal(@PathVariable("groupId") Long groupId, @PathVariable("goalId") Long goalId) {
        try {
            groupService.deleteGoal(groupId, goalId); // 서비스 계층에서 목표 삭제 처리
            return ResponseEntity.ok("Goal deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
