package com.example.LifeMaster_BE.Report;

/**
 * 신고 처리 상태를 나타내는 ENUM
 */
public enum ReportStatus {
    PENDING,    // 대기 중
    REVIEWING,  // 검토 중
    RESOLVED,   // 처리 완료
    REJECTED    // 반려됨
}
