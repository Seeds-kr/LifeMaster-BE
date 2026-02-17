package com.example.LifeMaster_BE.Report;

/**
 * 신고 처리 조치 유형을 나타내는 ENUM
 */
public enum ReportActionType {
    WARNING,       // 경고
    POST_DELETE,   // 게시글 삭제
    SUSPEND_1D,    // 1일 정지
    SUSPEND_7D,    // 7일 정지
    SUSPEND_30D    // 30일 정지
}
