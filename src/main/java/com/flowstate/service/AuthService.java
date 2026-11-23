package com.flowstate.service;

import com.flowstate.dto.LoginRequest;
import com.flowstate.dto.LoginResponse;
import com.flowstate.dto.RegisterRequest;
import com.flowstate.dto.RegisterResponse;
import com.flowstate.entity.User;
import com.flowstate.exception.EmailAlreadyExistsException;
import com.flowstate.repository.UserRepository;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
            return new RegisterResponse(
            savedUser.getEmail(),
            "Registration successful"
);
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtService.getExpiration()
            );
}
}