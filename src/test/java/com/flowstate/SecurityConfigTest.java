package com.flowstate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldEncodePassword() {
        String rawPassword = "Flowstate@2026";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void shouldRejectIncorrectPassword() {
        String encodedPassword = passwordEncoder.encode("Flowstate@2026");

        assertFalse(passwordEncoder.matches("WrongPassword@123", encodedPassword));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String firstHash = passwordEncoder.encode("Flowstate@2026");
        String secondHash = passwordEncoder.encode("Flowstate@2026");

        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordEncoder.matches("Flowstate@2026", firstHash));
        assertTrue(passwordEncoder.matches("Flowstate@2026", secondHash));
    }
}