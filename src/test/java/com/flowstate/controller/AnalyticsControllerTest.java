package com.flowstate.controller;

import com.flowstate.dto.LoginRequest;
import com.flowstate.dto.LoginResponse;
import com.flowstate.dto.RegisterRequest;
import com.flowstate.dto.TaskRequest;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;
import com.flowstate.service.AuthService;

import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String createUserAndGetToken(String email) {

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("Flowstate@2026");

        authService.register(request);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Flowstate@2026");

        LoginResponse response = authService.login(loginRequest);

        return response.getAccessToken();
    }

    private void createTask(
            String token,
            String title,
            boolean completed) throws Exception {

        TaskRequest request = new TaskRequest(
                title,
                "Analytics test task",
                completed,
                null
        );

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // NORMAL ANALYTICS TESTS

    @Test
    void shouldCalculateOverviewCorrectly() throws Exception {

        String token = createUserAndGetToken("analytics@example.com");

        createTask(token, "Completed task 1", true);
        createTask(token, "Completed task 2", true);
        createTask(token, "Pending task", false);

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(3))
                .andExpect(jsonPath("$.completedTasks").value(2))
                .andExpect(jsonPath("$.pendingTasks").value(1))
                .andExpect(jsonPath("$.completionRate").value(66.67));
    }

    @Test
    void shouldReturnDailyProductivity() throws Exception {

        String token = createUserAndGetToken("daily@example.com");

        createTask(token, "Completed task", true);
        createTask(token, "Pending task", false);

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/v1/analytics/daily")
                        .param("startDate", today)
                        .param("endDate", today)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].date").value(today))
                .andExpect(jsonPath("$[0].completedTasks").value(1));
    }

    @Test
    void shouldReturnWeeklyProductivity() throws Exception {

        String token = createUserAndGetToken("weekly@example.com");

        createTask(token, "Completed task", true);

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/v1/analytics/weekly")
                        .param("date", today)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompletedTasks").value(1))
                .andExpect(jsonPath("$.dailyBreakdown.length()").value(7));
    }

    @Test
    void shouldReturnUncategorizedTaskStatistics() throws Exception {

        String token = createUserAndGetToken("category@example.com");

        createTask(token, "Completed task", true);
        createTask(token, "Pending task", false);

        mockMvc.perform(get("/api/v1/analytics/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Uncategorized"))
                .andExpect(jsonPath("$[0].totalTasks").value(2))
                .andExpect(jsonPath("$[0].completedTasks").value(1))
                .andExpect(jsonPath("$[0].pendingTasks").value(1));
    }

    @Test
    void shouldOnlyReturnAuthenticatedUsersStatistics() throws Exception {

        String userAToken = createUserAndGetToken("usera-analytics@example.com");
        String userBToken = createUserAndGetToken("userb-analytics@example.com");

        createTask(userAToken, "User A completed task", true);
        createTask(userAToken, "User A pending task", false);

        createTask(userBToken, "User B completed task", true);

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(2))
                .andExpect(jsonPath("$.completedTasks").value(1));

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(1))
                .andExpect(jsonPath("$.completedTasks").value(1));
    }

    // EDGE-CASE TESTS

    @Test
    void shouldRejectDailyRangeWhenStartDateIsAfterEndDate() throws Exception {

        String token = createUserAndGetToken("invalidrange@example.com");

        LocalDate today = LocalDate.now();

        mockMvc.perform(get("/api/v1/analytics/daily")
                        .param("startDate", today.toString())
                        .param("endDate", today.minusDays(1).toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDailyRangeWithFutureEndDate() throws Exception {

        String token = createUserAndGetToken("futuredate@example.com");

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/v1/analytics/daily")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", tomorrow.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectDailyRangeExceeding90Days() throws Exception {

        String token = createUserAndGetToken("longrange@example.com");

        LocalDate today = LocalDate.now();

        mockMvc.perform(get("/api/v1/analytics/daily")
                        .param("startDate", today.minusDays(90).toString())
                        .param("endDate", today.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectWeeklyStatisticsForFutureDate() throws Exception {

        String token = createUserAndGetToken("futureweekly@example.com");

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        mockMvc.perform(get("/api/v1/analytics/weekly")
                        .param("date", tomorrow.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnZeroStatisticsWhenUserHasNoTasks() throws Exception {

        String token = createUserAndGetToken("emptyanalytics@example.com");

        mockMvc.perform(get("/api/v1/analytics/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasks").value(0))
                .andExpect(jsonPath("$.completedTasks").value(0))
                .andExpect(jsonPath("$.pendingTasks").value(0))
                .andExpect(jsonPath("$.completionRate").value(0.0));

        mockMvc.perform(get("/api/v1/analytics/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/v1/analytics/daily")
                        .param("startDate", today)
                        .param("endDate", today)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].completedTasks").value(0));

        mockMvc.perform(get("/api/v1/analytics/weekly")
                        .param("date", today)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompletedTasks").value(0))
                .andExpect(jsonPath("$.dailyBreakdown.length()").value(7));
    }
}