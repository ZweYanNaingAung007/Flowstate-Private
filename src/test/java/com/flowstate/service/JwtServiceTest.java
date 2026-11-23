package com.flowstate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    private JwtService jwtService;

    private final long expiration = 900000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtEncoder, expiration);
    }

    @Test
    void shouldGenerateTokenSuccessfully() {

        String email = "user@example.com";

        Jwt jwt = Jwt.withTokenValue("mock.jwt.token")
                .header("alg", "HS256")
                .subject(email)
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertEquals("mock.jwt.token", token);

        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void shouldGenerateTokenWithCorrectClaims() {

        String email = "user@example.com";

        Jwt jwt = Jwt.withTokenValue("mock.jwt.token")
                .header("alg", "HS256")
                .subject(email)
                .build();

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwt);

        jwtService.generateToken(email);

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(JwtEncoderParameters.class);

        verify(jwtEncoder).encode(captor.capture());

        JwtEncoderParameters parameters = captor.getValue();
        JwtClaimsSet claims = parameters.getClaims();

        assertEquals("flowstate", claims.getClaims().get("iss"));
        assertEquals(email, claims.getSubject());

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiresAt());

        assertEquals(
                expiration,
                claims.getExpiresAt().toEpochMilli()
                        - claims.getIssuedAt().toEpochMilli()
        );

        assertEquals(
                MacAlgorithm.HS256,
                parameters.getJwsHeader().getAlgorithm()
        );
    }

    @Test
    void shouldReturnExpirationInSeconds() {

        long result = jwtService.getExpiration();

        assertEquals(900, result);
    }
}