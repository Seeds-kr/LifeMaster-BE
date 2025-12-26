package com.example.LifeMaster_BE.TimeManager.Alarm;

import com.example.LifeMaster_BE.FunctionManager.Calender.ScheduleCalendarService;
import com.example.LifeMaster_BE.TimeManager.Alarm.Mapper.AlarmMapStruct;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.NewAlarmDto;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AlarmMapStruct alarmMapStruct;

    @InjectMocks
    private AlarmService alarmService;

    private final ScheduleCalendarService scheduleCalendarService;

    AlarmServiceTest(ScheduleCalendarService scheduleCalendarService) {
        this.scheduleCalendarService = scheduleCalendarService;
    }

    @Test
    @DisplayName("createAlarm: 멤버가 존재하면 알람을 저장하고 반환하며, 멤버에 연결하고 캘린더 이벤트를 갱신한다")
    void createAlarm_success_returnsSavedEntity_andLinksMember_andSyncsCalendar() {
        Long memberId = 10L;
        MemberEntity member = mock(MemberEntity.class);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        NewAlarmDto dto = mock(NewAlarmDto.class);

        AlarmEntity transientAlarm = mock(AlarmEntity.class);
        AlarmEntity savedAlarm = mock(AlarmEntity.class);
        when(savedAlarm.getId()).thenReturn(123L);

        // ✅ createAlarm 내부에서 alarmTime/요일 플래그를 참조하므로 스텁 필요
        // alarmTime은 "오늘 + 1시간" 정도로 잡아서 "현재 시간 이후" 조건을 쉽게 통과시키게 함
        ZoneId kst = ZoneId.of("Asia/Seoul");
        ZonedDateTime nowKst = ZonedDateTime.now(kst);
        LocalDateTime alarmTime = nowKst.plusHours(1).toLocalDateTime();
        when(savedAlarm.getAlarmTime()).thenReturn(alarmTime);

        // 요일 플래그: 전부 true면 이번 달 말까지 여러 번 호출될 수 있음(테스트는 횟수 고정 X)
        when(savedAlarm.isAlarmMon()).thenReturn(true);
        when(savedAlarm.isAlarmTue()).thenReturn(true);
        when(savedAlarm.isAlarmWed()).thenReturn(true);
        when(savedAlarm.isAlarmThu()).thenReturn(true);
        when(savedAlarm.isAlarmFri()).thenReturn(true);
        when(savedAlarm.isAlarmSat()).thenReturn(true);
        when(savedAlarm.isAlarmSun()).thenReturn(true);

        try (MockedStatic<AlarmEntity> mockedStatic = mockStatic(AlarmEntity.class)) {
            mockedStatic.when(() -> AlarmEntity.fromDto(dto)).thenReturn(transientAlarm);
            when(alarmRepository.save(transientAlarm)).thenReturn(savedAlarm);

            AlarmEntity result = alarmService.createAlarm(dto, memberId);

            assertSame(savedAlarm, result);
            assertEquals(123L, result.getId());

            verify(member).addAlarm(transientAlarm);
            verify(alarmRepository).save(transientAlarm);

            // 캘린더 이벤트는 "조건을 만족하는 날짜들"에 대해 여러 번 호출될 수 있으니
            // 최소 1번 이상 호출만 보장하는 방식으로 검증
            verify(scheduleCalendarService, atLeastOnce())
                    .addOrUpdateEvent(anyString(), eq("Alarm"));
        }
    }

    @Test
    @DisplayName("createAlarm: 멤버가 존재하지 않으면 EntityNotFoundException 발생")
    void createAlarm_memberNotFound_throws() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        NewAlarmDto dto = mock(NewAlarmDto.class);

        assertThrows(EntityNotFoundException.class, () -> alarmService.createAlarm(dto, 99L));
        verifyNoInteractions(alarmRepository);
    }

    @Test
    @DisplayName("getAllAlarms: 전체 알람을 조회해 DTO 리스트로 반환한다")
    void getAllAlarms_mapsEntitiesToDtos() {
        AlarmEntity a = mock(AlarmEntity.class);
        AlarmEntity b = mock(AlarmEntity.class);
        when(alarmRepository.findAll()).thenReturn(List.of(a, b));

        ResponseAlarmDto da = mock(ResponseAlarmDto.class);
        ResponseAlarmDto db = mock(ResponseAlarmDto.class);
        when(alarmMapStruct.toDtoList(List.of(a, b))).thenReturn(List.of(da, db));

        List<ResponseAlarmDto> result = alarmService.getAllAlarms();

        assertEquals(2, result.size());
        assertSame(da, result.get(0));
        assertSame(db, result.get(1));
    }

    @Test
    @DisplayName("getAlarmById: 해당 멤버의 알람을 조회해 DTO로 반환한다")
    void getAlarmById_found_returnsMappedDto() {
        Long alarmId = 1L, memberId = 7L;
        AlarmEntity alarm = mock(AlarmEntity.class);
        when(alarmRepository.findByIdAndMemberId(alarmId, memberId)).thenReturn(Optional.of(alarm));

        ResponseAlarmDto dto = mock(ResponseAlarmDto.class);
        when(alarmMapStruct.toDto(alarm)).thenReturn(dto);

        ResponseAlarmDto result = alarmService.getAlarmById(alarmId, memberId);

        assertSame(dto, result);
    }

    @Test
    @DisplayName("getAlarmById: 알람이 존재하지 않으면 EntityNotFoundException 발생")
    void getAlarmById_notFound_throws() {
        when(alarmRepository.findByIdAndMemberId(1L, 7L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> alarmService.getAlarmById(1L, 7L));
    }

    @Test
    @DisplayName("updateAlarmStatus: 알람 상태를 수정 후 저장한다")
    void updateAlarmStatus_found_setsStatusAndSaves() {
        Long alarmId = 5L;
        AlarmEntity alarm = mock(AlarmEntity.class);
        when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));

        alarmService.updateAlarmStatus(alarmId, true);

        verify(alarm).setAlarmStatus(true);
        verify(alarmRepository).save(alarm);
    }

    @Test
    @DisplayName("updateAlarmStatus: 알람이 존재하지 않으면 EntityNotFoundException 발생")
    void updateAlarmStatus_notFound_throws() {
        when(alarmRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> alarmService.updateAlarmStatus(404L, false));
    }

    @Test
    @DisplayName("updateAlarmDayStatus: 요일이 MON이면 setAlarmMon 호출")
    void updateAlarmDayStatus_setsCorrectDayFlag_Mon() {
        assertDaySetterCalled(AlarmDay.MON, "setAlarmMon");
    }

    @Test
    @DisplayName("updateAlarmDayStatus: 요일이 TUE이면 setAlarmTue 호출")
    void updateAlarmDayStatus_setsCorrectDayFlag_Tue() {
        assertDaySetterCalled(AlarmDay.TUE, "setAlarmTue");
    }

    @Test
    @DisplayName("updateAlarmDayStatus: 요일이 WED이면 setAlarmWed 호출")
    void updateAlarmDayStatus_setsCorrectDayFlag_Wed() {
        assertDaySetterCalled(AlarmDay.WED, "setAlarmWed");
    }

    @Test
    @DisplayName("updateAlarmDayStatus: 알람이 존재하지 않으면 EntityNotFoundException 발생")
    void updateAlarmDayStatus_notFound_throws() {
        when(alarmRepository.findById(8080L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> alarmService.updateAlarmDayStatus(8080L, AlarmDay.MON, true));
    }

    private void assertDaySetterCalled(AlarmDay day, String setterName) {
        Long alarmId = 100L;
        AlarmEntity alarm = mock(AlarmEntity.class);
        when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));

        alarmService.updateAlarmDayStatus(alarmId, day, true);

        switch (setterName) {
            case "setAlarmMon" -> verify(alarm).setAlarmMon(true);
            case "setAlarmTue" -> verify(alarm).setAlarmTue(true);
            case "setAlarmWed" -> verify(alarm).setAlarmWed(true);
            default -> fail("Unknown setter: " + setterName);
        }
        verify(alarmRepository).save(alarm);
    }

    @Test
    @DisplayName("deleteAlarm: 알람이 존재하면 삭제한다")
    void deleteAlarm_found_deletes() {
        Long alarmId = 55L;
        AlarmEntity alarm = mock(AlarmEntity.class);
        when(alarmRepository.findById(alarmId)).thenReturn(Optional.of(alarm));

        alarmService.deleteAlarm(alarmId);

        verify(alarmRepository).delete(alarm);
    }

    @Test
    @DisplayName("deleteAlarm: 알람이 존재하지 않으면 EntityNotFoundException 발생")
    void deleteAlarm_notFound_throws() {
        when(alarmRepository.findById(55L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> alarmService.deleteAlarm(55L));
        verify(alarmRepository, never()).delete(any());
    }
}