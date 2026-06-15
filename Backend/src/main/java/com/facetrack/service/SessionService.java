package com.facetrack.service;

import com.facetrack.dto.session.SessionCreateRequest;
import com.facetrack.dto.session.SessionResponse;
import com.facetrack.entity.*;
import com.facetrack.enums.AttendanceStatus;
import com.facetrack.enums.SessionStatus;
import com.facetrack.enums.UserRole;
import com.facetrack.exception.ConflictException;
import com.facetrack.exception.ForbiddenException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.AttendanceRepository;
import com.facetrack.repository.AttendanceSessionRepository;
import com.facetrack.repository.SchoolClassRepository;
import com.facetrack.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final AttendanceSessionRepository sessionRepository;
    private final SchoolClassRepository classRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    private void assertOwnerOrAdmin(AttendanceSession session, User user) {
        if (user.getRole() != UserRole.ADMIN
                && !session.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this session.");
        }
    }

    private void assertClassOwnerOrAdmin(SchoolClass sc, User user) {
        if (user.getRole() != UserRole.ADMIN
                && !sc.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not own this class.");
        }
    }

    public List<SessionResponse> list(UUID classId, User currentUser) {
        List<AttendanceSession> sessions;
        if (classId != null && currentUser.getRole() == UserRole.ADMIN) {
            sessions = sessionRepository.findBySchoolClassIdOrderByStartedAtDesc(classId);
        } else if (classId != null) {
            sessions = sessionRepository.findBySchoolClassIdAndTeacherIdOrderByStartedAtDesc(
                    classId, currentUser.getId());
        } else if (currentUser.getRole() == UserRole.ADMIN) {
            sessions = sessionRepository.findAllByOrderByStartedAtDesc();
        } else {
            sessions = sessionRepository.findByTeacherIdOrderByStartedAtDesc(currentUser.getId());
        }
        return sessions.stream().map(SessionResponse::from).toList();
    }

    public SessionResponse get(UUID sessionId, User currentUser) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        assertOwnerOrAdmin(session, currentUser);
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse start(SessionCreateRequest request, User currentUser) {
        SchoolClass sc = classRepository.findById(request.getClassId())
                .orElseThrow(() -> new NotFoundException("Class not found."));
        assertClassOwnerOrAdmin(sc, currentUser);

        // Prevent duplicate active sessions
        sessionRepository.findBySchoolClassIdAndStatus(request.getClassId(), SessionStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "An active session already exists for this class (id=" + existing.getId() + ").");
                });

        AttendanceSession session = AttendanceSession.builder()
                .schoolClass(sc)
                .teacher(currentUser)
                .note(request.getNote())
                .build();
        session = sessionRepository.save(session);
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse end(UUID sessionId, User currentUser) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        assertOwnerOrAdmin(session, currentUser);

        if (session.getStatus() == SessionStatus.ENDED) {
            return SessionResponse.from(session);
        }

        // Auto-mark absent students
        SchoolClass sc = session.getSchoolClass();
        List<Student> classStudents = studentRepository.findBySchoolClassId(sc.getId());
        List<Attendance> existingRecords = attendanceRepository.findBySessionId(session.getId());
        Set<UUID> presentIds = existingRecords.stream()
                .map(a -> a.getStudent().getId())
                .collect(Collectors.toSet());

        final AttendanceSession sessionRef = session;
        List<Attendance> absentRecords = classStudents.stream()
            .filter(s -> !presentIds.contains(s.getId()))
            .map(s -> Attendance.builder()
                .session(sessionRef)
                .student(s)
                .status(AttendanceStatus.ABSENT)
                .build())
            .toList();

        if (!absentRecords.isEmpty()) {
            attendanceRepository.saveAll(absentRecords);
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(Instant.now());
        session = sessionRepository.save(session);
        return SessionResponse.from(session);
    }
}
