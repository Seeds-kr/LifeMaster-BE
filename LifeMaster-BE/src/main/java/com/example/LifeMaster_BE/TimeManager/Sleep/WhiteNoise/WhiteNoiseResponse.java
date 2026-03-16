package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhiteNoiseResponse {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String audioUri;
    private String category;
}

