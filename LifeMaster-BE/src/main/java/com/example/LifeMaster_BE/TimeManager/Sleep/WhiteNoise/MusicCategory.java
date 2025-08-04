package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MusicCategory {
    WHITE_NOISE("백색소음"),
    NATURE_SOUNDS("자연의 소리"),
    CLASSICAL("클래식");

    private final String label;

    MusicCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static MusicCategory fromLabel(String label) {
        System.out.println("🎯 MusicCategory.fromLabel() 입력: " + label);

        for (MusicCategory category : MusicCategory.values()) {
            if (category.getLabel().equalsIgnoreCase(label.trim())) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + label);
    }
}
