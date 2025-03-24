package com.example.LifeMaster_BE.TimeManager.Sleep.WhiteNoise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WhiteNoiseDTO {
    private String title; // 백색소음 제목
    private String url; // 파일 경로 또는 스트리밍 URL
    private String length; //파일 재생 시간
}
