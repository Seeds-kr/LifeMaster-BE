package com.example.LifeMaster_BE.FunctionManager.Calender;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EventRequestDto {
    private String event;

    // ✅ 기본 생성자 추가
    public EventRequestDto() {
    }

    // (선택) 생성자 추가 – 테스트 코드나 수동 생성 시 편의
    public EventRequestDto(String event) {
        this.event = event;
    }

}