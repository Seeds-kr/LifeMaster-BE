package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhiteNoiseDTO {
    private Long id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String audioUri;
    private MusicCategory category;

    public static WhiteNoiseDTO fromEntity(WhiteNoiseEntity entity) {
        WhiteNoiseDTO dto = new WhiteNoiseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setAudioUri(entity.getAudioUri());
        dto.setCategory(entity.getCategory());
        return dto;
    }

    // Getter, Setter 생략
}