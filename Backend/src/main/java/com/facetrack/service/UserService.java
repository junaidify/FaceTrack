package com.facetrack.service;

import com.facetrack.dto.user.UserCreateRequest;
import com.facetrack.dto.user.UserResponse;
import com.facetrack.entity.User;
import com.facetrack.enums.UserRole;
import com.facetrack.exception.ConflictException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public List<UserResponse> listTeachers() {
        return userRepository.findByRole(UserRole.TEACHER).stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already in use.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
        userRepository.delete(user);
    }
}
