package com.example.LifeMaster_BE.TimeManager.Alarm.Dto.Mapper;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlarmMapStruct {

    ResponseAlarmDto toDto(AlarmEntity alarmEntity);

    List<ResponseAlarmDto> toDtoList(List<AlarmEntity> entities);
}
