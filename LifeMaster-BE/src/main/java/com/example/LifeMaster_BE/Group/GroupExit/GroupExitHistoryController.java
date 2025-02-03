package com.example.LifeMaster_BE.Group.GroupExit;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupExitHistoryController {

    private final GroupExitHistoryService exitHistoryService;

    public GroupExitHistoryController(GroupExitHistoryService exitHistoryService) {
        this.exitHistoryService = exitHistoryService;
    }

    @Operation(summary = "그룹별 유저 탈퇴 기록", description = "그룹별 유저 탈퇴 기록")
    @GetMapping("/{groupId}/recent-exits")
    public List<GroupExitHistoryEntity> getRecentExits(@PathVariable("groupId") Long groupId) {
        return exitHistoryService.getRecentExitsByGroup(groupId);
    }
}
