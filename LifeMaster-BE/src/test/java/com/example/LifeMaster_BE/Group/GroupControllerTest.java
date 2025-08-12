package com.example.LifeMaster_BE.Group;

import com.example.LifeMaster_BE.UserManager.Member.MemberEntity;
import com.example.LifeMaster_BE.UserManager.Member.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Sql(scripts = "/sql/clear-all.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class GroupControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private void ensureMemberExists(String email) {
        memberRepository.findByEmail(email).orElseGet(() -> {
            MemberEntity m = new MemberEntity();
            m.setEmail(email);
            // 필요한 널 불가 필드가 있으면 여기서 세팅
            return memberRepository.save(m);
        });
    }

    // --- 유틸: 회원가입/로그인 후 JWT 발급 ---
    private String registerAndLogin(String email) throws Exception {
        // 회원가입
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "Test1234!",
                              "passwordConfirm": "Test1234!"
                            }
                            """.formatted(email)))
                .andExpect(status().isOk());

        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "Test1234!"
                            }
                            """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getContentAsString().replace("\"", "");
    }

    // --- 유틸: 그룹 생성 후 ID 추출 ---
    private long createGroupAndGetId(String jwt, String name) throws Exception {
        MvcResult create = mockMvc.perform(multipart("/group/create")
                        .param("name", name)
                        .param("description", "desc")
                        .param("icon", "https://icon")
                        // statistics, password는 선택 파라미터이므로 생략
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        JsonNode node = objectMapper.readTree(create.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    @Test
    @DisplayName("그룹 생성 후 전체 조회")
    void createAndListGroups() throws Exception {
        String email = "owner@example.com";
        String jwt = registerAndLogin(email);

        // (선택) 혹시 회원이 없다면 생성 보장
        memberRepository.findByEmail(email).orElseGet(() -> {
            MemberEntity m = new MemberEntity();
            m.setEmail(email);
            return memberRepository.save(m);
        });

        // ✅ 여기서 실제 PK를 조회 (지금 상황에선 2가 나올 것)
        Long memberId = memberRepository.findByEmail(email)
                .orElseThrow()
                .getId();

        createGroupAndGetId(jwt, "MyGroup");

        mockMvc.perform(get("/group")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MyGroup"))
                // 필요하면 생성자 id까지 검증
                .andExpect(jsonPath("$[0].creator.id").value(memberId.intValue()));
    }

    @Test
    @DisplayName("그룹 단건 조회")
    void getGroupById() throws Exception {
        String jwt = registerAndLogin("owner2@example.com");
        long groupId = createGroupAndGetId(jwt, "Alpha");

        mockMvc.perform(get("/group/{id}", groupId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("Alpha"));
    }

    @Test
    @DisplayName("그룹 수정")
    void updateGroup() throws Exception {
        String jwt = registerAndLogin("owner3@example.com");
        long groupId = createGroupAndGetId(jwt, "Before");

        mockMvc.perform(put("/group/{id}", groupId)
                        .param("name", "After")
                        .param("description", "updated")
                        .param("icon", "https://newicon")
                        // statistics, password 생략 가능
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After"))
                .andExpect(jsonPath("$.description").value("updated"));
    }

    @Test
    @DisplayName("그룹에 목표 추가")
    void addGoalToGroup() throws Exception {
        String jwt = registerAndLogin("owner4@example.com");
        long groupId = createGroupAndGetId(jwt, "GoalGroup");

        String goalJson = """
            {
              "name": "Study",
              "goalCondition": "time",
              "duration": "weekly",
              "value": 600
            }
            """;

        mockMvc.perform(post("/group/{groupId}/goal", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalJson)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals[0].name").value("Study"))
                .andExpect(jsonPath("$.goals[0].goalCondition").value("time"));
    }

    @Test
    @DisplayName("그룹 목표 삭제")
    void deleteGoalFromGroup() throws Exception {
        String jwt = registerAndLogin("owner5@example.com");
        long groupId = createGroupAndGetId(jwt, "GoalDelGroup");

        // 목표 추가 먼저
        MvcResult addRes = mockMvc.perform(post("/group/{groupId}/goal", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Run",
                              "goalCondition": "count",
                              "duration": "daily",
                              "value": 30
                            }
                            """)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode groupAfterAdd = objectMapper.readTree(addRes.getResponse().getContentAsString());
        long goalId = groupAfterAdd.get("goals").get(0).get("id").asLong();

        // 목표 삭제
        mockMvc.perform(delete("/group/{groupId}/goal/{goalId}", groupId, goalId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Goal deleted successfully."));
    }

    @Test
    @DisplayName("그룹 사용자 목록 조회 (생성자 1명 포함)")
    void getUsersByGroup() throws Exception {
        String jwt = registerAndLogin("owner6@example.com");
        long groupId = createGroupAndGetId(jwt, "UserListGroup");

        mockMvc.perform(get("/group/{groupId}/users", groupId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("owner6@example.com"));
    }

    @Test
    @DisplayName("그룹 삭제")
    void deleteGroup() throws Exception {
        String jwt = registerAndLogin("owner7@example.com");
        long groupId = createGroupAndGetId(jwt, "ToBeDeleted");

        mockMvc.perform(delete("/group/{id}", groupId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isNoContent());

        // 삭제 확인
        mockMvc.perform(get("/group/{id}", groupId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().is4xxClientError());
    }

    // ---------------------------------------------------------
    // 필요시 확장 테스트 (컨트롤러/도메인 준비 상태에 따라 성공/실패 가능)
    // ---------------------------------------------------------

    @Test
    @DisplayName("통계 목표 추가/삭제 (확장)")
    void addAndRemoveStatisticGoal() throws Exception {
        String jwt = registerAndLogin("owner8@example.com");
        long groupId = createGroupAndGetId(jwt, "StatGroup");

        // 목표 추가
        MvcResult addGoal = mockMvc.perform(post("/group/{groupId}/goal", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "name": "Sleep",
                              "goalCondition": "time",
                              "duration": "weekly",
                              "value": 420
                            }
                            """)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();
        long goalId = objectMapper.readTree(addGoal.getResponse().getContentAsString())
                .get("goals").get(0).get("id").asLong();

        // 통계 표시 목표 추가
        mockMvc.perform(post("/group/{groupId}/addStatisticGoal/{goalId}", groupId, goalId)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statistics[0]").value(goalId));

        // 통계에서 제거
        mockMvc.perform(delete("/group/{groupId}/statistic", groupId)
                        .param("statistic", String.valueOf(goalId))
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk());
    }
}
