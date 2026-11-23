package com.flowstate.service;

import com.flowstate.dto.RegisterRequest;
import com.flowstate.dto.RegisterResponse;
import com.flowstate.entity.User;
import com.flowstate.exception.EmailAlreadyExistsException;
import com.flowstate.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;


    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        when(userRepository.existsByEmail("user@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("Flowstate@2026"))
                .thenReturn("bcrypt-hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse registeredUser = authService.register(request);

        assertNotNull(registeredUser);
        assertEquals("user@example.com", registeredUser.getEmail());
        assertEquals("Registration successful", registeredUser.getMessage());

        verify(userRepository).save(any(User.class));
    }


    @Test
    void shouldStoreEncodedPasswordInsteadOfRawPassword() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        when(passwordEncoder.encode("Flowstate@2026"))
                .thenReturn("bcrypt-hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                !user.getPassword().equals("Flowstate@2026")
                && user.getPassword().equals("bcrypt-hashed-password")
        ));

        verify(passwordEncoder).encode("Flowstate@2026");
    }


    @Test
    void shouldNormalizeEmailBeforeRegistration() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("  USER@EXAMPLE.COM  ");
        request.setPassword("Flowstate@2026");

        when(userRepository.existsByEmail("user@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("bcrypt-hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterResponse registeredUser = authService.register(request);

        assertEquals("user@example.com", registeredUser.getEmail());

        verify(userRepository).existsByEmail("user@example.com");
    }


    @Test
    void shouldRejectRegistrationWithExistingEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Flowstate@2026");

        when(userRepository.existsByEmail("user@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}