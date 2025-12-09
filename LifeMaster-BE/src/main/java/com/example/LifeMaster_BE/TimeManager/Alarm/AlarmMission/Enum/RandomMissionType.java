package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum;

public enum RandomMissionType {

    MATH_PROBLEM("수학 문제"),
    TYPING_SENTENCE("문장 따라쓰기"),
    FOLLOW_CLICK("따라 누르기"),
    NONE("미션 없음"); // ← 다시 추가됨!

    private final String displayName;

    RandomMissionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 문자열을 enum으로 변환.
     * null, "none", "", 찾을 수 없는 값 → null 반환
     * (NONE은 내부 용도이며 문자열 입력으로 생성되지 않음)
     */
    public static RandomMissionType fromString(String value) {
        if (value == null) return null;

        String v = value.trim().toLowerCase();
        if (v.isEmpty() || v.equals("none")) return null;  // NONE도 문자열 입력 시 null 반환

        return switch (v) {
            case "math", "math_problem" -> MATH_PROBLEM;
            case "typing", "typing_sentence" -> TYPING_SENTENCE;
            case "follow", "follow_click" -> FOLLOW_CLICK;
            default -> null;
        };
    }
}
