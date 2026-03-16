package com.example.LifeMaster_BE.Challenge.Detox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepeatDetoxService {

    private final RepeatDetoxRepository repeatDetoxRepository;

    // 반복 잠금 생성
    public void createRepeatDetox(RepeatDetoxDto.Request request) {

        RepeatDetox repeatDetox = RepeatDetox.builder()
                .lockedApp(request.getLockedApp())
                .sessionUsageLimit(request.getSessionUsageLimit())
                .lockDuration(request.getLockDuration())
                .dailyMaxUsageLimit(request.getDailyMaxUsageLimit())
                .build();

        repeatDetoxRepository.save(repeatDetox);
    }

    // 전체 반복 잠금 조회
    public RepeatDetoxDto.ListResponse getAllRepeatDetox() {

        List<RepeatDetoxDto.Response> list =
                repeatDetoxRepository.findAll()
                        .stream()
                        .map(detox -> RepeatDetoxDto.Response.builder()
                                .id(detox.getId())
                                .lockedApp(detox.getLockedApp())
                                .sessionUsageLimit(detox.getSessionUsageLimit())
                                .lockDuration(detox.getLockDuration())
                                .dailyMaxUsageLimit(detox.getDailyMaxUsageLimit())
                                .build())
                        .collect(Collectors.toList());

        return RepeatDetoxDto.ListResponse.builder()
                .lockedApps(list)
                .build();
    }

    // 반복 잠금 삭제
    public void deleteRepeatDetox(Long id) {
        repeatDetoxRepository.deleteById(id);
    }
}