package com.example.LifeMaster_BE;

import com.example.LifeMaster_BE.FunctionManager.Calender.CalendarController;
import com.example.LifeMaster_BE.FunctionManager.Calender.CalendarEntity;
import com.example.LifeMaster_BE.FunctionManager.Calender.CalendarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(CalendarController.class)
public class CalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CalendarService calendarService;

    private CalendarEntity testEntry;

    @BeforeEach
    public void setUp() {
        testEntry = new CalendarEntity();
        testEntry.setId(1L);
        testEntry.setDate("20231110");
        testEntry.setDay("금요일");
        testEntry.setEvents(new ArrayList<>(Arrays.asList("Meeting", "Workshop")));
    }

    @Test
    public void testGetAllEntries() throws Exception {
        List<CalendarEntity> entries = Arrays.asList(testEntry);
        Mockito.when(calendarService.findAll()).thenReturn(entries);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("20231110"))
                .andExpect(jsonPath("$[0].events[0]").value("Meeting"));
    }

    @Test
    public void testGetEntriesByDate() throws Exception {
        List<CalendarEntity> entries = Arrays.asList(testEntry);
        Mockito.when(calendarService.findByDate("20231110")).thenReturn(entries);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/calendar/20231110"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("20231110"))
                .andExpect(jsonPath("$[0].events[1]").value("Workshop"));
    }

    @Test
    public void testGetEntriesByMonth() throws Exception {
        List<CalendarEntity> entries = Arrays.asList(testEntry);
        Mockito.when(calendarService.findByMonth("202311")).thenReturn(entries);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/calendar/month/202311"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("20231110"));
    }

    @Test
    public void testCreateEvent() throws Exception {
        Mockito.when(calendarService.createEvent(anyString(), any())).thenReturn(testEntry);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/calendar/create")
                        .param("date", "20231110")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Conference\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20231110"))
                .andExpect(jsonPath("$.events[0]").value("Meeting"));
    }

    @Test
    public void testAddEvent() throws Exception {
        Mockito.when(calendarService.addOrUpdateEvent(anyString(), anyString())).thenReturn(testEntry);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/calendar/20231110/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Workshop\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20231110"))
                .andExpect(jsonPath("$.events[1]").value("Workshop"));
    }

    @Test
    public void testDeleteEventByDate() throws Exception {
        Mockito.when(calendarService.deleteEventByDate("20231110")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/calendar/20231110"))
                .andExpect(status().isOk())
                .andExpect(content().string("삭제되었습니다."));
    }

    @Test
    public void testDeleteSpecificEvent() throws Exception {
        Mockito.when(calendarService.deleteSpecificEvent(anyString(), anyString())).thenReturn(testEntry);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/calendar/20231110/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Meeting\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("20231110"))
                .andExpect(jsonPath("$.events[0]").value("Meeting"));
    }
}

