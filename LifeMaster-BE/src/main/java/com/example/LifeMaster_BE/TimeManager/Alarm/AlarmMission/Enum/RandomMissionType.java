package com.example.LifeMaster_BE.TimeManager.Alarm.AlarmMission.Enum;

public enum RandomMissionType {

    MATH_PROBLEM("수학 문제"),
    TYPING_SENTENCE("문장 따라쓰기"),
    FOLLOW_CLICK("따라 누르기"),
    NONE("미션 없음");

    private final String displayName;

    RandomMissionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RandomMissionType fromString(String value) {
        if (value == null) return NONE;

        return switch (value.trim().toLowerCase()) {
            case "math", "math_problem" -> MATH_PROBLEM;
            case "typing", "typing_sentence" -> TYPING_SENTENCE;
            case "follow", "follow_click" -> FOLLOW_CLICK;
            default -> NONE;
        };
    }
}
