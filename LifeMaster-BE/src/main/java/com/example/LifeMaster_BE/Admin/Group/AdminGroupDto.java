package com.example.LifeMaster_BE.Admin.Group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminGroupDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Summary {
        private long totalGroups;
        private long emptyGroups;
        private long privateGroups;
        private long abnormalGroups;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GroupListItem {
        private Long groupId;
        private String name;
        private String description;
        private String accessType;
        private Long ownerId;
        private String ownerEmail;
        private int memberCount;
        private int goalCount;
        private boolean abnormal;
        private String abnormalReason;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GroupMemberItem {
        private Long memberId;
        private String email;
        private String nickname;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GroupActivity {
        private Long groupId;
        private String groupName;
        private int memberCount;
        private int goalCount;
        private int statisticGoalCount;
        private boolean abnormal;
        private String abnormalReason;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class GroupDashboard {
        private Summary summary;
        private List<GroupListItem> groups;
    }
}