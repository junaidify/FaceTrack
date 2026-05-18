package com.facetrack.service;

import com.facetrack.dto.auth.LoginRequest;
import com.facetrack.dto.auth.TokenResponse;
import com.facetrack.entity.User;
import com.facetrack.exception.AuthException;
import com.facetrack.repository.UserRepository;
import com.facetrack.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password."));

        if (!user.isActive()) {
            throw new AuthException("Invalid email or password.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password.");
        }

        String token = jwtService.createAccessToken(user.getId(), user.getRole().name().toLowerCase());

        return TokenResponse.builder()
                .accessToken(token)
                .role(user.getRole())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
