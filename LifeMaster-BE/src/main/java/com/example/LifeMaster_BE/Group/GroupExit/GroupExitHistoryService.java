package com.example.LifeMaster_BE.Group.GroupExit;

import com.example.LifeMaster_BE.Group.GroupEntity;
import com.example.LifeMaster_BE.Group.GroupRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupExitHistoryService {

    private final GroupExitHistoryRepository exitHistoryRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public GroupExitHistoryService(GroupExitHistoryRepository exitHistoryRepository, GroupRepository groupRepository, MemberRepository memberRepository) {
        this.exitHistoryRepository = exitHistoryRepository;
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void recordGroupExit(Long groupId, Long memberId) {
        GroupEntity group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + memberId));

        GroupExitHistoryEntity exitHistory = new GroupExitHistoryEntity(group, member);
        exitHistoryRepository.save(exitHistory);
    }

    public List<GroupExitHistoryEntity> getRecentExitsByGroup(Long groupId) {
        return exitHistoryRepository.findByGroupIdOrderByExitTimeDesc(groupId);
    }

    public void deleteByGroupId(Long groupId) {
        exitHistoryRepository.deleteByGroupId(groupId);
    }
}
