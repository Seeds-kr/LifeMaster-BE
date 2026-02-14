package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.Group.Goal.GoalDTO;
import com.example.LifeMaster_BE.Group.Goal.GoalEntity;
import com.example.LifeMaster_BE.Security.CustomUserDetails;
import com.example.LifeMaster_BE.UserManager.Login;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final Login login;



    @Operation(summary = "Create a new group", description = "Creates a new group and associates it with the creator.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupEntity.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input provided")
    })
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "icon", required = false) String icon,
            @RequestParam(value = "통계 표시할 목표(null 이면 전체 표시)", required = false) List<Long> statistics,
            @RequestParam(value = "password", required = false) String password,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long creatorId = user.getId();
        String creatorEmail = user.getUsername(); // 일반적으로 이메일

        GroupEntity group = groupService.createGroup(
                name, description, icon, statistics, password, creatorId, creatorEmail
        );
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Get all groups", description = "Retrieves a list of all groups.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of groups")
    })
    @GetMapping
    public ResponseEntity<?> getAllGroups(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        List<GroupResponseDto> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get group by ID", description = "Retrieves the details of a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved group details"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(
            @Parameter(description = "ID of the group") @PathVariable("id") Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        GroupResponseDto group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "사용자가 속한 그룹들을 반환", description = "사용자가 속한 그룹들을 반환")
    @GetMapping("/user/me")
    public ResponseEntity<?> getMyGroups(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;

        Long memberId = user.getId();

        List<GroupResponseDto> groups = groupService.getGroupsByUser(memberId);
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Update group details", description = "Updates the details of a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(
            @Parameter(description = "ID of the group to update") @PathVariable("id") Long id,
            @Parameter(description = "Name of the group") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Description of the group") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Icon URL of the group") @RequestParam(value = "icon", required = false) String icon,
            @Parameter(description = "통계 표시할 목표(null 이면 전체 표시)") @RequestParam(value = "statistics", required = false) List<Long> statistics,
            @Parameter(description = "Password for the group") @RequestParam(value = "password", required = false) String password,
            @AuthenticationPrincipal CustomUserDetails user)
    {ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        GroupEntity group = groupService.updateGroup(id, name, description, icon, statistics, password);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Delete a group", description = "Deletes a specific group by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@Parameter(description = "ID of the group to delete") @PathVariable("id") Long id,@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Add a goal to a group", description = "목표 이름/기준(goalCondition)(time/count)/기한(duration)(daily/weekly/monthly)/목표값(value)(예: 7시간, 50회 등) 입력")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Goal added successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/goal")
    public ResponseEntity<?> addGoalToGroup(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @RequestBody GoalDTO goalDTO,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
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
    public ResponseEntity<?> deleteGoal(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @Parameter(description = "ID of the goal") @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
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
    public ResponseEntity<?> addUserToGroup(
            @Parameter(description = "ID of the group") @PathVariable("groupId") Long groupId,
            @Parameter(description = "ID of the user") @PathVariable("userId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        String response = groupService.addUserToGroup(groupId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a user to a group", description = "Deletes a specific user to a group.")
    @DeleteMapping("/{groupId}/user/{userId}")
    public ResponseEntity<?> removeUserFromGroup(@PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId,@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        groupService.removeUserFromGroup(groupId, userId);
        return ResponseEntity.ok("User removed from group successfully.");
    }

    // 목표 ID를 그룹의 통계에 추가하는 API
    @Operation(summary = "Add a statisticGoal to show", description = "통계 표시할 목표 추가")
    @PostMapping("/{groupId}/addStatisticGoal/{goalId}")
    public ResponseEntity<?> addStatisticGoal(
            @PathVariable("groupId") Long groupId,
            @PathVariable("goalId") Long goalId,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        GroupEntity updatedGroup = groupService.addStatisticGoal(groupId, goalId);
        return ResponseEntity.ok(updatedGroup);
    }

    // 그룹에 속한 사용자들을 반환하는 API
    @Operation(summary = "그룹에 속한 사용자들을 반환", description = "그룹에 속한 사용자들을 반환")
    @GetMapping("/{groupId}/users")
    public ResponseEntity<?> getUsersByGroup(@PathVariable("groupId") Long groupId,@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        List<MemberEntity> users = groupService.getUsersByGroup(groupId);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "사용자가 속한 그룹의 목표 조회", description = "현재 로그인한 사용자가 속한 그룹의 목표 목록을 반환합니다.")
    @GetMapping("/goals")
    public ResponseEntity<?> getGoalsByUser(@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        if (user == null) {
            return ResponseEntity.status(401).build(); // 로그인되지 않은 경우 401 Unauthorized 반환
        }
        Long memberId = user.getId();
        List<GoalEntity> goals = groupService.getGoalsByMemberId(memberId);
        return ResponseEntity.ok(goals);
    }

    // 그룹에서 통계 항목 삭제
    @Operation(summary = "그룹에서 통계 항목 삭제", description = "그룹에서 통계 항목 삭제")
    @DeleteMapping("/{groupId}/statistic")
    public ResponseEntity<?> removeStatisticFromGroup(
            @PathVariable("groupId") Long groupId,
            @RequestParam("statistic") Long statistic,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        GroupEntity updatedGroup = groupService.removeStatisticFromGroup(groupId, statistic);
        return ResponseEntity.ok(updatedGroup);
    }

    @Operation(summary = "그룹 목표별 진행률", description = "그룹 목표별 진행률")
    @GetMapping("/{groupId}/goals/progress")
    public ResponseEntity<?> getGroupGoalProgress(@PathVariable("groupId") Long groupId,@AuthenticationPrincipal CustomUserDetails user) {
        List<Map<String, Object>> groupProgress = groupService.getGroupGoalProgress(groupId);
        return ResponseEntity.ok(groupProgress);
    }

    // ✅ 초대 코드 생성 API
    @Operation(summary = "초대 코드 생성", description = "그룹 ID/그룹 비밀번호 기반 초대 코드 생성")
    @GetMapping("/{groupId}/invite")
    public ResponseEntity<?> generateInviteCode(@PathVariable("groupId") Long groupId,@AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        String inviteCode = groupService.generateInviteCode(groupId);
        return ResponseEntity.ok(inviteCode);
    }

    // ✅ 초대 코드로 그룹 가입 API
    @Operation(summary = "초대 코드로 그룹 가입", description = "초대 코드로 그룹 가입")
    @PostMapping("/join")
    public ResponseEntity<?> joinGroupWithInviteCode(
            @RequestParam("userId") Long userId,
            @RequestParam("inviteCode") String inviteCode,
            @AuthenticationPrincipal CustomUserDetails user) {
        ResponseEntity<?> loginCheck = login.checkLogin(user);
        if (loginCheck != null) return loginCheck;
        String response = groupService.joinGroupWithInviteCode(userId, inviteCode);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "그룹 통계", description = "일주일치 수면 통계 반환")
    @GetMapping("/{groupId}/sleep-stats")
    public ResponseEntity<GroupDto.Static> getUserSleepStats(
            @AuthenticationPrincipal UserDetails userdetails,
            @PathVariable Long groupId) {

        String email = userdetails.getUsername();

        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다."));

        GroupDto.Static sleepStats = groupService.getUserStatic(email, group);
        return ResponseEntity.ok(sleepStats);
    }
}
