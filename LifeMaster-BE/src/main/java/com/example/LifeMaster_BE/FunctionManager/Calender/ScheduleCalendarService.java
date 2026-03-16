package com.example.LifeMaster_BE.FunctionManager.Calender;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoEntity;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoRepository;
import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("scheduleCalendarService")
public class ScheduleCalendarService {

    private final ScheduleCalendarRepository calendarRepository;
    private final TodoRepository todoRepository;
    private final EntityManager em;

    public ScheduleCalendarService(
            ScheduleCalendarRepository calendarRepository,
            TodoRepository todoRepository,
            EntityManager em
    ) {
        this.calendarRepository = calendarRepository;
        this.todoRepository = todoRepository;
        this.em = em;
    }

    // ✅ (중요) memberId 기준 전체 조회
    public List<ScheduleCalendarEntity> findAll(Long memberId) {
        return calendarRepository.findAllByMemberId(memberId);
    }

    // ✅ memberId + 특정 날짜 조회
    public Optional<ScheduleCalendarEntity> findByDate(Long memberId, String date) {
        return calendarRepository.findByMemberIdAndDate(memberId, date);
    }

    // ✅ memberId + 월별 조회 (date: yyyyMMdd or yyyymm..)
    public List<ScheduleCalendarEntity> findByMonth(Long memberId, String date) {
        if (date != null && date.length() >= 6) {
            String month = date.substring(0, 6); // 'YYYYMM'
            return calendarRepository.findByMemberIdAndDateStartingWith(memberId, month);
        }
        return new ArrayList<>();
    }

    // ✅ 새 항목 생성 (memberId 포함)
    public ScheduleCalendarEntity createEvent(Long memberId, String date, List<String> events) {
        ScheduleCalendarEntity entry = new ScheduleCalendarEntity();

        String realDate = Objects.requireNonNullElseGet(date, ScheduleCalendarService::getTodayDate);

        entry.setMember(getMemberRef(memberId));
        entry.setDate(realDate);
        entry.setDay(getDayOfWeek(realDate));
        entry.setEvents(events);

        return calendarRepository.save(entry);
    }

    // ✅ 엔티티 통째 생성 (memberId 포함, 날짜 중복: member+date로 검사)
    public ScheduleCalendarEntity createCalendarEntity(Long memberId, ScheduleCalendarEntity calendarEntity) {
        String realDate = Objects.requireNonNullElseGet(calendarEntity.getDate(), ScheduleCalendarService::getTodayDate);

        Optional<ScheduleCalendarEntity> existingEntry =
                calendarRepository.findByMemberIdAndDate(memberId, realDate);

        if (existingEntry.isPresent()) {
            throw new IllegalArgumentException("Calendar entry for the given member/date already exists.");
        }

        ScheduleCalendarEntity entry = new ScheduleCalendarEntity();
        entry.setMember(getMemberRef(memberId));
        entry.setDate(realDate);
        entry.setDay(getDayOfWeek(realDate));
        entry.setEvents(calendarEntity.getEvents());
        entry.setTodos(calendarEntity.getTodos());

        return calendarRepository.save(entry);
    }

    // ✅ 특정 날짜에 이벤트 추가/업데이트 (memberId 포함)
    public ScheduleCalendarEntity addOrUpdateEvent(Long memberId, String date, String event) {
        if (date == null || date.isBlank()) {
            date = getTodayDate();
        }

        String finalDate = date;
        ScheduleCalendarEntity entry = calendarRepository
                .findByMemberIdAndDate(memberId, date)
                .orElseGet(() -> {
                    ScheduleCalendarEntity e = new ScheduleCalendarEntity();
                    e.setMember(getMemberRef(memberId));
                    e.setDate(finalDate);

                    LocalDate localDate = LocalDate.parse(finalDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    e.setDay(localDate.getDayOfWeek().name()); // 예: MONDAY

                    e.setEvents(new ArrayList<>());
                    return e;
                });

        if (entry.getEvents() == null) entry.setEvents(new ArrayList<>());

        if (event != null && !event.isBlank() && !entry.getEvents().contains(event)) {
            entry.getEvents().add(event);
        }

        return calendarRepository.save(entry);
    }

    // ✅ 날짜 엔트리 삭제 (memberId 포함)
    public boolean deleteEventByDate(Long memberId, String date) {
        Optional<ScheduleCalendarEntity> entries = calendarRepository.findByMemberIdAndDate(memberId, date);
        if (entries.isPresent()) {
            calendarRepository.delete(entries.get());
            return true;
        }
        return false;
    }

    // ✅ 특정 이벤트만 삭제 (memberId 포함)
    public ScheduleCalendarEntity deleteSpecificEvent(Long memberId, String date, String event) {
        Optional<ScheduleCalendarEntity> entries = calendarRepository.findByMemberIdAndDate(memberId, date);
        if (entries.isEmpty()) return null;

        ScheduleCalendarEntity entry = entries.get();

        if (entry.getEvents() == null) return entry;

        entry.getEvents().remove(event);

        if (entry.getEvents().isEmpty()) {
            calendarRepository.delete(entry);
            return entry;
        }

        return calendarRepository.save(entry);
    }

    // ✅ 날짜로 요일 구하기
    public static String getDayOfWeek(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        return dayOfWeek.toString();
    }

    // ✅ 오늘 날짜 yyyyMMdd
    public static String getTodayDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return today.format(formatter);
    }

    // ✅ 특정 날짜의 todo (memberId 포함)
    public List<TodoEntity> getTodosForDate(Long memberId, String date) {
        return todoRepository.findByMemberIdAndDate(memberId, date);
    }

    // ✅ FK 연결용 Member 프록시
    private MemberEntity getMemberRef(Long memberId) {
        // DB hit 최소화: 프록시 참조로 연결
        return em.getReference(MemberEntity.class, memberId);
    }
}
