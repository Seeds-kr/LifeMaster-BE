package com.example.LifeMaster_BE.TimeManager.Alarm.Mapper;

import com.example.LifeMaster_BE.TimeManager.Alarm.AlarmEntity;
import com.example.LifeMaster_BE.TimeManager.Alarm.Dto.ResponseAlarmDto;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-28T01:32:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.5 (Amazon.com Inc.)"
)
@Component
public class AlarmMapStructImpl implements AlarmMapStruct {

    @Override
    public ResponseAlarmDto toDto(AlarmEntity alarmEntity) {
        if ( alarmEntity == null ) {
            return null;
        }

        ResponseAlarmDto responseAlarmDto = new ResponseAlarmDto();

        responseAlarmDto.setAlarmTitle( alarmEntity.getAlarmTitle() );
        responseAlarmDto.setAlarmTime( alarmEntity.getAlarmTime() );
        responseAlarmDto.setAlarmMon( alarmEntity.isAlarmMon() );
        responseAlarmDto.setAlarmTue( alarmEntity.isAlarmTue() );
        responseAlarmDto.setAlarmWed( alarmEntity.isAlarmWed() );
        responseAlarmDto.setAlarmThu( alarmEntity.isAlarmThu() );
        responseAlarmDto.setAlarmFri( alarmEntity.isAlarmFri() );
        responseAlarmDto.setAlarmSat( alarmEntity.isAlarmSat() );
        responseAlarmDto.setAlarmSun( alarmEntity.isAlarmSun() );
        responseAlarmDto.setAlarmSound( alarmEntity.getAlarmSound() );
        responseAlarmDto.setSnoozed( alarmEntity.isSnoozed() );
        responseAlarmDto.setSnoozeTime( alarmEntity.getSnoozeTime() );
        responseAlarmDto.setSnoozeCount( alarmEntity.getSnoozeCount() );
        responseAlarmDto.setReSlept( alarmEntity.isReSlept() );
        responseAlarmDto.setReSleptTime( alarmEntity.getReSleptTime() );
        responseAlarmDto.setRandomMissionType( alarmEntity.getRandomMissionType() );

        return responseAlarmDto;
    }

    @Override
    public List<ResponseAlarmDto> toDtoList(List<AlarmEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<ResponseAlarmDto> list = new ArrayList<ResponseAlarmDto>( entities.size() );
        for ( AlarmEntity alarmEntity : entities ) {
            list.add( toDto( alarmEntity ) );
        }

        return list;
    }
}
