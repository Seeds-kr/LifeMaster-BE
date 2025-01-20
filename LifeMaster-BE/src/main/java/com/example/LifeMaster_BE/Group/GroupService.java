package com.example.LifeMaster_BE.Group;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
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
}
