package com.facetrack.service;

import com.facetrack.dto.user.UserCreateRequest;
import com.facetrack.dto.user.UserResponse;
import com.facetrack.entity.User;
import com.facetrack.enums.UserRole;
import com.facetrack.exception.ConflictException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.SchoolClassRepository;
import com.facetrack.repository.AttendanceSessionRepository;
import com.facetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolClassRepository classRepository;
    private final AttendanceSessionRepository sessionRepository;

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

    @Transactional
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // Block deletion if the user is a teacher who owns classes or active sessions
        if (!classRepository.findByTeacherId(userId).isEmpty()) {
            throw new ConflictException("Cannot delete user: they are assigned as teacher to one or more classes. Reassign or delete those classes first.");
        }
        if (!sessionRepository.findByTeacherIdOrderByStartedAtDesc(userId).isEmpty()) {
            throw new ConflictException("Cannot delete user: they own one or more attendance sessions.");
        }

        userRepository.delete(user);
    }
}
