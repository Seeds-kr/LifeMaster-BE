package com.example.LifeMaster_BE.Group;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
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
    public ResponseEntity<GroupEntity> getGroupById(@PathVariable Long id) {
        GroupEntity group = groupService.getGroupById(id);
        return ResponseEntity.ok(group);
    }

    // Update a group by ID
    @PutMapping("/{id}")
    public ResponseEntity<GroupEntity> updateGroup(@PathVariable Long id, @RequestBody GroupEntity updatedGroup) {
        GroupEntity group = groupService.updateGroup(id, updatedGroup);
        return ResponseEntity.ok(group);
    }

    // Delete a group by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }
}
