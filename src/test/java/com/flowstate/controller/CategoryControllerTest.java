package com.flowstate.controller;

import com.flowstate.dto.CategoryRequest;
import com.flowstate.dto.LoginRequest;
import com.flowstate.dto.LoginResponse;
import com.flowstate.dto.RegisterRequest;
import com.flowstate.repository.CategoryRepository;
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
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        categoryRepository.deleteAll();
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

    private long createCategory(String token, String name)
            throws Exception {

        CategoryRequest request = new CategoryRequest(
                name,
                "Test category description"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        return response.get("categoryId").asLong();
    }

    @Test
    void shouldCreateCategorySuccessfully() throws Exception {

        String token = createUserAndGetToken("categoryuser@example.com");

        CategoryRequest request = new CategoryRequest(
                "Programming",
                "Programming-related tasks"
        );

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.name").value("Programming"))
                .andExpect(jsonPath("$.description")
                        .value("Programming-related tasks"));
    }

    @Test
    void shouldRejectCategoryWithBlankName() throws Exception {

        String token = createUserAndGetToken("categoryuser@example.com");

        CategoryRequest request = new CategoryRequest(
                " ",
                "Test description"
        );

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCategoryWithDescriptionExceeding255Characters()
            throws Exception {

        String token = createUserAndGetToken("categoryuser@example.com");

        CategoryRequest request = new CategoryRequest(
                "Programming",
                "A".repeat(256)
        );

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRetrieveExistingCategory() throws Exception {

        String token = createUserAndGetToken("categoryuser@example.com");

        long categoryId = createCategory(token, "Programming");

        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .param("id", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andExpect(jsonPath("$.name").value("Programming"));
    }

    @Test
    void shouldReturnNotFoundForNonexistentCategory() throws Exception {

        String token = createUserAndGetToken("categoryuser@example.com");

        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .param("id", "999999"))
                .andExpect(status().isNotFound());
    }
}