package com.example.LifeMaster_BE.TimeManager.Sleep;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SleepService {

    private final SleepRepository sleepRepository;
    private final MemberRepository userRepository;

    // 수면 기록 완료 이벤트 발행용
    private final ApplicationEventPublisher eventPublisher;


    /**
     * 수면 시작 기록 생성
     */
    @Transactional
    public void makeSleep(
            SleepDto.Request request,
            Long userId
    ) {

        MemberEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 사용자입니다."
                        )
                );


        // AlarmSettings 변환
        AlarmSettings alarmSettings = null;
        Boolean isWakeUpAlarmSet = null;


        if (request.getAlarmInfo() != null) {

            isWakeUpAlarmSet =
                    request.getAlarmInfo()
                            .getIsWakeUpAlarmSet();


            if (Boolean.TRUE.equals(isWakeUpAlarmSet)
                    && request.getAlarmInfo()
                    .getAlarmSettings() != null) {

                SleepDto.AlarmSettingsDto dto =
                        request.getAlarmInfo()
                                .getAlarmSettings();


                alarmSettings =
                        AlarmSettings.builder()
                                .alarmSnoozeCnt(
                                        dto.getAlarmSnoozeCnt()
                                )
                                .timeToWakeUp(
                                        dto.getTimeToWakeUp()
                                )
                                .antiSleepMode(
                                        dto.getAntiSleepMode()
                                )
                                .build();
            }
        }


        Sleep sleep =
                Sleep.builder()
                        .sleepDate(
                                request.getSleepDate()
                        )
                        .sleepStart(
                                request.getSleepStart()
                        )
                        .sleepEnd(
                                request.getSleepEnd()
                        )
                        .sleepMood(
                                request.getSleepMood()
                        )
                        .isWakeUpAlarmSet(
                                isWakeUpAlarmSet
                        )
                        .alarmSettings(
                                alarmSettings
                        )
                        .user(
                                user
                        )
                        .build();


        /*
         * 수면 점수 계산
         */
        double sleepScore =
                calculateSleepScore(sleep);

        sleep.setSleepScore(
                sleepScore
        );


        /*
         * 수면 기록 저장
         */
        Sleep saved =
                sleepRepository.save(sleep);


        /*
         * 수면 기록 이벤트 발생
         *
         * SleepRecordedEventListener에서
         * AFTER_COMMIT으로 처리됨
         *
         * 즉 현재 Transaction이 정상적으로
         * Commit된 이후 그룹 SLEEP 목표를 검사
         */
        eventPublisher.publishEvent(
                new SleepRecordedEvent(
                        userId,
                        saved.getSleepDate()
                )
        );
    }


    /**
     * 수면 점수 계산 로직
     */
    private double calculateSleepScore(
            Sleep sleep
    ) {

        if (sleep.getSleepStart() == null
                || sleep.getSleepEnd() == null) {

            throw new IllegalArgumentException(
                    "수면 시작/종료 시간이 필요합니다."
            );
        }


        long sleepDuration =
                Duration.between(
                        sleep.getSleepStart(),
                        sleep.getSleepEnd()
                ).toHours();


        double durationScore;


        if (sleepDuration >= 7
                && sleepDuration <= 9) {

            durationScore = 100;

        } else if (sleepDuration < 7) {

            durationScore =
                    100 * (
                            sleepDuration / 7.0
                    );

        } else {

            durationScore =
                    100 * (
                            9.0 / sleepDuration
                    );
        }


        double moodScore =
                switch (sleep.getSleepMood()) {

                    case VERY_GOOD ->
                            1.0;

                    case GOOD ->
                            0.85;

                    case BAD ->
                            0.6;

                    case VERY_BAD ->
                            0.4;
                };


        int snoozeCnt =
                (
                        sleep.getAlarmSettings() != null
                                && sleep.getAlarmSettings()
                                .getAlarmSnoozeCnt() != null
                )
                        ? sleep.getAlarmSettings()
                        .getAlarmSnoozeCnt()
                        : 0;


        double snoozePenalty =
                Math.max(
                        1.0 - (
                                0.05 * snoozeCnt
                        ),
                        0.5
                );


        int timeToWakeUp =
                (
                        sleep.getAlarmSettings() != null
                                && sleep.getAlarmSettings()
                                .getTimeToWakeUp() != null
                )
                        ? sleep.getAlarmSettings()
                        .getTimeToWakeUp()
                        : 0;


        double wakeUpPenalty =
                timeToWakeUp <= 10
                        ? 1.0
                        : Math.max(
                        1.0
                                - (
                                timeToWakeUp - 10
                        ) * 0.02,
                        0.6
                );


        double antiSleepBonus =
                (
                        sleep.getAlarmSettings() != null
                                && Boolean.TRUE.equals(
                                sleep.getAlarmSettings()
                                        .getAntiSleepMode()
                        )
                )
                        ? 1.05
                        : 1.0;


        double rawScore =
                durationScore
                        * moodScore
                        * snoozePenalty
                        * wakeUpPenalty
                        * antiSleepBonus;


        return Math.min(
                Math.max(
                        rawScore,
                        0
                ),
                100
        );
    }


    /**
     * 수면 데이터 수정
     */
    @Transactional
    public SleepDto.Response updateSleep(
            SleepDto.Request request,
            Long userId
    ) {

        Sleep existingSleep =
                sleepRepository.findById(
                                request.getSleepId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 수면 데이터입니다."
                                )
                        );


        MemberEntity user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 사용자입니다."
                                )
                        );


        /*
         * AlarmInfo 업데이트
         */
        AlarmSettings alarmSettings =
                existingSleep.getAlarmSettings();

        Boolean isWakeUpAlarmSet =
                existingSleep.getIsWakeUpAlarmSet();


        if (request.getAlarmInfo() != null) {

            isWakeUpAlarmSet =
                    request.getAlarmInfo()
                            .getIsWakeUpAlarmSet();


            if (Boolean.TRUE.equals(isWakeUpAlarmSet)
                    && request.getAlarmInfo()
                    .getAlarmSettings() != null) {

                SleepDto.AlarmSettingsDto dto =
                        request.getAlarmInfo()
                                .getAlarmSettings();


                alarmSettings =
                        AlarmSettings.builder()
                                .alarmSnoozeCnt(
                                        dto.getAlarmSnoozeCnt()
                                )
                                .timeToWakeUp(
                                        dto.getTimeToWakeUp()
                                )
                                .antiSleepMode(
                                        dto.getAntiSleepMode()
                                )
                                .build();

            } else {

                alarmSettings = null;
            }
        }


        Sleep updatedSleep =
                existingSleep.toBuilder()
                        .user(user)

                        .sleepDate(
                                request.getSleepDate() != null
                                        ? request.getSleepDate()
                                        : existingSleep.getSleepDate()
                        )

                        .sleepStart(
                                request.getSleepStart() != null
                                        ? request.getSleepStart()
                                        : existingSleep.getSleepStart()
                        )

                        .sleepEnd(
                                request.getSleepEnd() != null
                                        ? request.getSleepEnd()
                                        : existingSleep.getSleepEnd()
                        )

                        .sleepMood(
                                request.getSleepMood() != null
                                        ? request.getSleepMood()
                                        : existingSleep.getSleepMood()
                        )

                        .isWakeUpAlarmSet(
                                isWakeUpAlarmSet
                        )

                        .alarmSettings(
                                alarmSettings
                        )

                        .build();


        /*
         * 수정된 수면 정보 기준으로
         * 수면 점수 재계산
         */
        double sleepScore =
                calculateSleepScore(
                        updatedSleep
                );

        updatedSleep.setSleepScore(
                sleepScore
        );


        /*
         * 수정 저장
         */
        Sleep saved =
                sleepRepository.save(
                        updatedSleep
                );


        /*
         * 수면 기록 수정 후에도 이벤트 발생
         *
         * 예:
         *
         * 기존 7시간
         * ↓
         * 8시간으로 수정
         * ↓
         * SLEEP 목표 달성 가능
         */
        eventPublisher.publishEvent(
                new SleepRecordedEvent(
                        userId,
                        saved.getSleepDate()
                )
        );


        return toResponse(saved);
    }


    /**
     * 수면 기록 조회
     */
    @Transactional(readOnly = true)
    public List<SleepDto.Response> selectSleep(
            Long userId
    ) {

        LocalDate oneWeekAgo =
                LocalDate.now()
                        .minusDays(7);


        MemberEntity user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 사용자입니다."
                                )
                        );


        List<Sleep> sleepList =
                sleepRepository
                        .findByUserAndSleepDateAfter(
                                user,
                                oneWeekAgo
                        );


        return sleepList.stream()
                .map(this::toResponse)
                .toList();
    }


    /**
     * 엔티티 -> DTO 변환
     */
    private SleepDto.Response toResponse(
            Sleep sleep
    ) {

        SleepDto.AlarmSettingsDto alarmSettingsDto =
                null;


        if (sleep.getAlarmSettings() != null) {

            alarmSettingsDto =
                    SleepDto.AlarmSettingsDto.builder()

                            .alarmSnoozeCnt(
                                    sleep.getAlarmSettings()
                                            .getAlarmSnoozeCnt()
                            )

                            .timeToWakeUp(
                                    sleep.getAlarmSettings()
                                            .getTimeToWakeUp()
                            )

                            .antiSleepMode(
                                    sleep.getAlarmSettings()
                                            .getAntiSleepMode()
                            )

                            .build();
        }


        SleepDto.AlarmInfoDto alarmInfoDto =
                SleepDto.AlarmInfoDto.builder()

                        .isWakeUpAlarmSet(
                                sleep.getIsWakeUpAlarmSet()
                        )

                        .alarmSettings(
                                alarmSettingsDto
                        )

                        .build();


        return SleepDto.Response.builder()

                .sleepId(
                        sleep.getSleepId()
                )

                .sleepDate(
                        sleep.getSleepDate()
                )

                .sleepStart(
                        sleep.getSleepStart()
                )

                .sleepEnd(
                        sleep.getSleepEnd()
                )

                .sleepMood(
                        sleep.getSleepMood()
                )

                .alarmInfo(
                        alarmInfoDto
                )

                .sleepScore(
                        sleep.getSleepScore()
                )

                .build();
    }
}