package com.example.LifeMaster_BE.FunctionManager.Calender;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service("scheduleCalendarService")
public class ScheduleCalendarService {

    private final ScheduleCalendarRepository calendarRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    public ScheduleCalendarService(ScheduleCalendarRepository calendarRepository) {
        this.calendarRepository = calendarRepository;
    }

    // 전체 조회
    public List<ScheduleCalendarEntity> findAll() {
        return calendarRepository.findAll();
    }

    // 특정 날짜 조회
    public List<ScheduleCalendarEntity> findByDate(String date) {
        return calendarRepository.findByDate(date);
    }

    // 월별 조회 (yyyy-MM 형식의 month 값)
    public List<ScheduleCalendarEntity> findByMonth(String date) {
        // 입력된 date의 'YYYYMMDD' 형식에서 'YYYYMM' 형식으로 변환
        if (date.length() >= 6) {
            String month = date.substring(0, 6); // 'YYYYMM'만 추출
            return calendarRepository.findByDateStartingWith(month);
        }
        // 만약 입력이 'YYYYMMDD' 형식이 아니면 빈 리스트 반환
        return new ArrayList<>();
    }

    // 새 항목 추가 (생성)
    public ScheduleCalendarEntity createEvent(String date, List<String> events) {
        ScheduleCalendarEntity entry = new ScheduleCalendarEntity();
        String day = getDayOfWeek(date);
        entry.setDay(day);
        //날짜가 있으면 해당 날짜로, 아니면 오늘 날짜로 할당
        entry.setDate(Objects.requireNonNullElseGet(date, ScheduleCalendarService::getTodayDate));
        entry.setEvents(events);
        return calendarRepository.save(entry);
    }

    public ScheduleCalendarEntity createCalendarEntity(ScheduleCalendarEntity calendarEntity) {
        ScheduleCalendarEntity entry = new ScheduleCalendarEntity();
        String day = getDayOfWeek(calendarEntity.getDate());
        entry.setDay(day);
        //날짜가 있으면 해당 날짜로, 아니면 오늘 날짜로 할당
        entry.setDate(Objects.requireNonNullElseGet(calendarEntity.getDate(), ScheduleCalendarService::getTodayDate));
        entry.setEvents(calendarEntity.getEvents());
        entry.setToDoList(calendarEntity.getToDoList());
        return calendarRepository.save(entry);
    }

    // 특정 날짜에 항목 추가 또는 업데이트
    public ScheduleCalendarEntity addOrUpdateEvent(String date, String event) {
        List<ScheduleCalendarEntity> entries = calendarRepository.findByDate(date);
        ScheduleCalendarEntity entry;
        if (entries.isEmpty()) {
            entry = new ScheduleCalendarEntity();
            entry.setDate(date);
        } else {
            entry = entries.get(0);
        }
        entry.getEvents().add(event);
        return calendarRepository.save(entry);
    }

    // 날짜 전체 삭제
    public boolean deleteEventByDate(String date) {
        List<ScheduleCalendarEntity> entries = calendarRepository.findByDate(date);
        if (!entries.isEmpty()) {
            ScheduleCalendarEntity entry = entries.get(0);
            calendarRepository.delete(entry);
            List<TodoEntity> todoEntries = todoRepository.findByDate(date);
            if (!entries.isEmpty()) {
                TodoEntity todoEntry = todoEntries.get(0);
                todoRepository.delete(todoEntry);
            }
            return true;
        }
        return false;
    }

    // 특정 항목 삭제
    public ScheduleCalendarEntity deleteSpecificEvent(String date, String event) {
        List<ScheduleCalendarEntity> entries = calendarRepository.findByDate(date);
        if (!entries.isEmpty()) {
            ScheduleCalendarEntity entry = entries.get(0);
            entry.getEvents().remove(event);
            return calendarRepository.save(entry);
        }
        return null;
    }

    //날짜로 요일을 구하는 메소드
    public static String getDayOfWeek(String date) {
        // "YYYYMMDD" 형식의 문자열을 LocalDate 객체로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(date, formatter);

        // 요일 구하기
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();

        // 요일 이름 반환 (한국어 요일로 반환하려면 아래 주석 참고)
        return dayOfWeek.toString();
    }

    // 오늘 날짜를 "YYYYMMDD" 형식의 문자열로 반환하는 메서드
    public static String getTodayDate() {
        LocalDate today = LocalDate.now(); // 오늘 날짜 가져오기
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return today.format(formatter);
    }

    //특정 날짜의 ToDoList를 반환하는 메소드
    public List<TodoEntity> getTodosForDate(String date) {
        return todoRepository.findByDate(date);
    }
}

