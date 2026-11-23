package com.flowstate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.flowstate.dto.RegisterRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldAcceptValidEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "email");

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "email");

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectEmptyEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "email");

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectEmailExceeding254Characters() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a".repeat(245) + "@gmail.com");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "email");

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptStrongPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("Flowstate@2026");

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "password");

        assertTrue(violations.isEmpty());
}

    @Test
    void shouldRejectPasswordWithoutUppercase() {
        assertInvalidPassword("flowstate@2026");
}

    @Test
    void shouldRejectPasswordWithoutLowercase() {
        assertInvalidPassword("FLOWSTATE@2026");
}

    @Test
    void shouldRejectPasswordWithoutDigit() {
        assertInvalidPassword("Flowstate@");
}

    @Test
    void shouldRejectPasswordWithoutSpecialCharacter() {
        assertInvalidPassword("Flowstate2026");
}

    @Test
    void shouldRejectPasswordShorterThanEightCharacters() {
        assertInvalidPassword("Abc@123");
}

    @Test
    void shouldRejectEmptyPassword() {
        assertInvalidPassword("");
}

private void assertInvalidPassword(String password) {
        RegisterRequest request = new RegisterRequest();
        request.setPassword(password);

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validateProperty(request, "password");

        assertFalse(violations.isEmpty());
}}