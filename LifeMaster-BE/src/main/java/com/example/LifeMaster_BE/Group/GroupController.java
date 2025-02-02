package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalDTO;
import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Group.GoalProgress.GoalProgressService;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService, GoalProgressService goalProgressService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Create a new group", description = "Creates a new group and associates it with the creator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    @PostMapping("/create")
    public ResponseEntity<GroupEntity> createGroup(
            @Parameter(description = "Name of the group") @RequestParam("name") String name,
            @Parameter(description = "Description of the group") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Icon URL of the group") @RequestParam(value = "icon", required = false) String icon,
            @Parameter(description = "통계 표시할 목표(null 이면 전체 표시)") @RequestParam(value = "statistics", required = false) List<Long> statistics,
            @Parameter(description = "Password for the group") @RequestParam(value = "password", required = false) String password,
            @Parameter(description = "ID of the creator") @RequestParam("creatorId") Long creatorId) {
        GroupEntity group = groupService.createGroup(name, description, icon, statistics, password, creatorId);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Get all groups", description = "Retrieves a list of all groups.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of groups")
    })
    @GetMapping
    public ResponseEntity<List<GroupEntity>> getAllGroups() {
        List<GroupEntity> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get group by ID", description = "Retrieves the details of a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved group details"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<GroupEntity> getGroupById(@Parameter(description = "ID of the group") @PathVariable("id") Long id) {
        GroupEntity group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Update group details", description = "Updates the details of a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GroupEntity> updateGroup(
            @Parameter(description = "ID of the group to update") @PathVariable("id") Long id,
            @Parameter(description = "Name of the group") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Description of the group") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Icon URL of the group") @RequestParam(value = "icon", required = false) String icon,
            @Parameter(description = "통계 표시할 목표(null 이면 전체 표시)") @RequestParam(value = "statistics", required = false) List<Long> statistics,
            @Parameter(description = "Password for the group") @RequestParam(value = "password", required = false) String password)
            {
        GroupEntity group = groupService.updateGroup(id, name, description, icon, statistics, password);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Delete a group", description = "Deletes a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@Parameter(description = "ID of the group to delete") @PathVariable("id") Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add a goal to a group", description = "목표 이름/기준(goalCondition)(time/count)/기한(duration)(daily/weekly/monthly)/목표값(value)(예: 7시간, 50회 등) 입력")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal added successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/goal")
    public ResponseEntity<GroupEntity> addGoalToGroup(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @RequestBody GoalDTO goalDTO) {
        GroupEntity group = groupService.findById(groupId);
        GoalEntity goal = new GoalEntity();

        goal.setName(goalDTO.getName());
        goal.setGoalCondition(goalDTO.getGoalCondition());
        goal.setDuration(goalDTO.getDuration());
        goal.setValue(goalDTO.getValue());
        goal.setGroup(group);

        GroupEntity updatedGroup = groupService.addGoalToGroup(groupId, goal);
        return ResponseEntity.ok(updatedGroup);
    }

    @Operation(summary = "Delete a goal from a group", description = "Deletes a specific goal from a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group or goal not found")
    })
    @DeleteMapping("/{groupId}/goal/{goalId}")
    public ResponseEntity<String> deleteGoal(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @Parameter(description = "ID of the goal") @PathVariable("goalId") Long goalId) {
        try {
            groupService.deleteGoal(groupId, goalId);
            return ResponseEntity.ok("Goal deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Add a user to a group", description = "Adds a specific user to a group.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{groupId}/addUser/{userId}")
    public ResponseEntity<String> addUserToGroup(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @Parameter(description = "ID of the user") @PathVariable("userId") Long userId) {
        String response = groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(response);
    }

    // 목표 ID를 그룹의 통계에 추가하는 API
    @Operation(summary = "Add a statisticGoal to show", description = "통계 표시할 목표 추가")
    @PostMapping("/{groupId}/addStatisticGoal/{goalId}")
    public ResponseEntity<GroupEntity> addStatisticGoal(
            @PathVariable("groupId") Long groupId,
            @PathVariable("goalId") Long goalId) {
        GroupEntity updatedGroup = groupService.addStatisticGoal(groupId, goalId);
        return ResponseEntity.ok(updatedGroup);
    }

    // 사용자가 속한 그룹들을 반환하는 API
    @Operation(summary = "사용자가 속한 그룹들을 반환", description = "사용자가 속한 그룹들을 반환")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupEntity>> getGroupsByUser(@PathVariable("userId") Long userId) {
        List<GroupEntity> groups = groupService.getGroupsByUser(userId);
        return ResponseEntity.ok(groups);
    }

    // 그룹에 속한 사용자들을 반환하는 API
    @Operation(summary = "그룹에 속한 사용자들을 반환", description = "그룹에 속한 사용자들을 반환")
    @GetMapping("/{groupId}/users")
    public ResponseEntity<List<MemberEntity>> getUsersByGroup(@PathVariable("groupId") Long groupId) {
        List<MemberEntity> users = groupService.getUsersByGroup(groupId);
        return ResponseEntity.ok(users);
    }

    // 그룹에서 통계 항목 삭제
    @Operation(summary = "그룹에서 통계 항목 삭제", description = "그룹에서 통계 항목 삭제")
    @DeleteMapping("/{groupId}/statistic")
    public ResponseEntity<GroupEntity> removeStatisticFromGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam("statistic") Long statistic) {

        GroupEntity updatedGroup = groupService.removeStatisticFromGroup(groupId, statistic);
        return ResponseEntity.ok(updatedGroup);
    }

    @GetMapping("/{groupId}/goals/progress")
    public List<Map<String, Object>> getGroupGoalProgress(@PathVariable("groupId") Long groupId) {
        return groupService.getGroupGoalProgress(groupId);
    }

    // ✅ 초대 코드 생성 API
    @Operation(summary = "초대 코드 생성", description = "그룹 ID/그룹 비밀번호 기반 초대 코드 생성")
    @GetMapping("/{groupId}/invite")
    public ResponseEntity<String> generateInviteCode(@PathVariable("groupId") Long groupId) {
        String inviteCode = groupService.generateInviteCode(groupId);
        return ResponseEntity.ok(inviteCode);
    }

    // ✅ 초대 코드로 그룹 가입 API
    @Operation(summary = "초대 코드로 그룹 가입", description = "초대 코드로 그룹 가입")
    @PostMapping("/join")
    public ResponseEntity<String> joinGroupWithInviteCode(
            @RequestParam("userId") Long userId,
            @RequestParam("inviteCode") String inviteCode) {
        String response = groupService.joinGroupWithInviteCode(userId, inviteCode);
        return ResponseEntity.ok(response);
    }
}
