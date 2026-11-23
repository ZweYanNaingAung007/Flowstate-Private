package com.flowstate.controller;

import com.flowstate.dto.LoginRequest;
import com.flowstate.dto.LoginResponse;
import com.flowstate.dto.RegisterRequest;
import com.flowstate.dto.TaskRequest;
import com.flowstate.entity.Task;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;
import com.flowstate.service.AuthService;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

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

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword("Flowstate@2026");

        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("Flowstate@2026");

        LoginResponse loginResponse = authService.login(loginRequest);

        return loginResponse.getAccessToken();
    }

    private long createTask(String token, String title) throws Exception {

        TaskRequest request = new TaskRequest(
                title,
                "Test task description",
                false,
                null
        );

        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("id").asLong();
    }

    @Test
    void shouldAllowUserToAccessOwnTask() throws Exception {

        String token = createUserAndGetToken("usera@example.com");

        long taskId = createTask(token, "User A Task");

        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("User A Task"));
    }

    @Test
    void shouldPreventUserFromAccessingAnotherUsersTask() throws Exception {

        String userAToken = createUserAndGetToken("usera@example.com");
        String userBToken = createUserAndGetToken("userb@example.com");

        long taskId = createTask(userAToken, "Private Task");

        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPreventUserFromUpdatingAnotherUsersTask() throws Exception {

        String userAToken = createUserAndGetToken("usera@example.com");
        String userBToken = createUserAndGetToken("userb@example.com");

        long taskId = createTask(userAToken, "Original Task");

        TaskRequest updateRequest = new TaskRequest(
                "Modified Task",
                "Modified description",
                true,
                null
        );

        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        Task storedTask = taskRepository.findById(taskId).orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "Original Task", storedTask.getTitle()
        );
    }

    @Test
    void shouldPreventUserFromDeletingAnotherUsersTask() throws Exception {

        String userAToken = createUserAndGetToken("usera@example.com");
        String userBToken = createUserAndGetToken("userb@example.com");

        long taskId = createTask(userAToken, "Protected Task");

        mockMvc.perform(delete("/api/v1/tasks/" + taskId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        Task storedTask = taskRepository.findById(taskId).orElseThrow();

        org.junit.jupiter.api.Assertions.assertFalse(storedTask.isDeleted());
    }

    @Test
    void shouldOnlyReturnTasksOwnedByAuthenticatedUser() throws Exception {

        String userAToken = createUserAndGetToken("usera@example.com");
        String userBToken = createUserAndGetToken("userb@example.com");

        createTask(userAToken, "User A Task");
        createTask(userBToken, "User B Task");

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks.length()").value(1))
                .andExpect(jsonPath("$.tasks[0].title").value("User A Task"));
    }
}