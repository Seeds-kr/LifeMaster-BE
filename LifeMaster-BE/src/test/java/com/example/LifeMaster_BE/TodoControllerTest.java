package com.example.LifeMaster_BE;

import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoCreateRequest;
import com.example.LifeMaster_BE.FunctionManager.ToDoList.TodoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TodoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;
    private Long todoId;

    @BeforeEach
    void setUp() throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!",
                      "passwordConfirm": "Test1234!"
                    }
                """)).andExpect(status().isOk());

        // 로그인
        MvcResult result = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "testuser@example.com",
                      "password": "Test1234!"
                    }
                """)).andExpect(status().isOk()).andReturn();

        jwtToken = result.getResponse().getContentAsString().replace("\"", "");

        // Todo 생성
        TodoCreateRequest todoCreateRequest = new TodoCreateRequest();
        todoCreateRequest.setTitle("기본 할일");
        todoCreateRequest.setDate("20250701");

        MvcResult createResult = mockMvc.perform(post("/schedule/todo/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoCreateRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String content = createResult.getResponse().getContentAsString();
        TodoResponse createdTodo = objectMapper.readValue(content, TodoResponse.class);
        todoId = createdTodo.getId();
    }

    @Test
    @DisplayName("모든 Todo 조회")
    void testGetAllTodos() throws Exception {
        mockMvc.perform(get("/schedule/todo")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("특정 ID로 Todo 조회")
    void testGetTodoById() throws Exception {
        mockMvc.perform(get("/schedule/todo/" + todoId)
                        .param("title", "기본 할일")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("기본 할일"));
    }

    @Test
    @DisplayName("Todo 수정")
    void testUpdateTodo() throws Exception {
        mockMvc.perform(put("/schedule/todo/{id}", todoId)
                        .param("title", "수정된 할일")
                        .param("date", "20250702")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 할일"))
                .andExpect(jsonPath("$.date").value("20250702"));
    }

    @Test
    @DisplayName("완료 상태 토글")
    void testToggleCompleted() throws Exception {
        mockMvc.perform(patch("/schedule/todo/" + todoId + "/toggle-completed")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true)); // 첫 상태는 false
    }

    @Test
    @DisplayName("유저의 Todo 조회")
    void testGetTodosByMember() throws Exception {
        mockMvc.perform(get("/schedule/todo/member/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Todo 삭제")
    void testDeleteTodo() throws Exception {
        mockMvc.perform(delete("/schedule/todo/" + todoId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/schedule/todo")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
