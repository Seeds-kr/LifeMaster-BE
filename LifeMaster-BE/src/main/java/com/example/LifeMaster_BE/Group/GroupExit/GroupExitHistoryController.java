package com.example.LifeMaster_BE.Group.GroupExit;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupExitHistoryController {

    private final GroupExitHistoryService exitHistoryService;

    public GroupExitHistoryController(GroupExitHistoryService exitHistoryService) {
        this.exitHistoryService = exitHistoryService;
    }

    @GetMapping("/{groupId}/recent-exits")
    public List<GroupExitHistoryEntity> getRecentExits(@PathVariable("groupId") Long groupId) {
        return exitHistoryService.getRecentExitsByGroup(groupId);
    }
}
