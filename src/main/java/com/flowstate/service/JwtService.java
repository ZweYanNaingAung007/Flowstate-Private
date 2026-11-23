package com.flowstate.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiration;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${flowstate.jwt.expiration}") long expiration) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public String generateToken(String email) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("flowstate")
                .subject(email)
                .issuedAt(now)
                .expiresAt(now.plusMillis(expiration))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long getExpiration() {
        return expiration / 1000;
    }
}