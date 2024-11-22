package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class WhiteNoiseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 백색소음 제목
    private String url; // 파일 경로 또는 스트리밍 URL
    private String length; //파일 재생 시간
}

