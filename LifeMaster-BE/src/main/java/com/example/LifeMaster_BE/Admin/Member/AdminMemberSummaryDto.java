package com.example.LifeMaster_BE.Admin.Member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMemberSummaryDto {

    private long totalMemberCount;
    private long activeMemberCount;
    private long premiumMemberCount;
    private long adminMemberCount;
}