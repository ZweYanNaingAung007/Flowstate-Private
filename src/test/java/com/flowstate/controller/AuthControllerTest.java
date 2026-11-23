package com.flowstate.controller;

import tools.jackson.databind.ObjectMapper;

import com.flowstate.dto.LoginRequest;
import com.flowstate.dto.RegisterRequest;
import com.flowstate.entity.User;
import com.flowstate.repository.TaskRepository;
import com.flowstate.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    // REGISTRATION TESTS

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    void shouldRejectDuplicateEmail() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidRegistrationRequest() throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");
        request.setPassword("weak");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // LOGIN TESTS

    @Test
    void shouldLoginSuccessfully() throws Exception {

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("Flowstate@2026"));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));
    }

    @Test
    void shouldRejectIncorrectPassword() throws Exception {

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("Flowstate@2026"));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("WrongPassword@2026");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownEmail() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("Flowstate@2026");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectInvalidLoginRequest() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
