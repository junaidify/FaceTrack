package com.facetrack.service;

import com.facetrack.config.AppProperties;
import com.facetrack.dto.attendance.AttendanceMarkRequest;
import com.facetrack.dto.attendance.AttendanceMarkResponse;
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

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final FaceClientService faceClientService;
    private final AppProperties appProperties;

    @Transactional
    public AttendanceMarkResponse mark(AttendanceMarkRequest request, User currentUser) {
        AttendanceSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new NotFoundException("Session not found."));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ConflictException("Session is not active.");
        }
        if (currentUser.getRole() != UserRole.ADMIN
                && !session.getTeacher().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not own this session.");
        }

        SchoolClass sc = session.getSchoolClass();
        if (sc == null) {
            throw new NotFoundException("Class for this session no longer exists.");
        }

        // Get enrolled students in this class
        List<Student> enrolledStudents = studentRepository.findEnrolledByClassId(sc.getId());
        if (enrolledStudents.isEmpty()) {
            return AttendanceMarkResponse.builder().matched(false).build();
        }

        List<List<Double>> candidates = enrolledStudents.stream()
                .map(Student::getFaceEmbedding)
                .toList();

        double threshold = appProperties.getFaceService().getMatchThreshold();
        FaceClientService.MatchResult result = faceClientService.match(
                request.getImage(), candidates, threshold);

        if (!result.isMatched() || result.getBestIndex() == null) {
            return AttendanceMarkResponse.builder()
                    .matched(false)
                    .similarity(result.getSimilarity())
                    .build();
        }

        Student matchedStudent = enrolledStudents.get(result.getBestIndex());

        // Check for duplicate marking
        return attendanceRepository.findBySessionIdAndStudentId(
                session.getId(), matchedStudent.getId())
                .map(existing -> AttendanceMarkResponse.builder()
                        .matched(true)
                        .studentId(matchedStudent.getId())
                        .studentName(matchedStudent.getName())
                        .rollNo(matchedStudent.getRollNo())
                        .similarity(result.getSimilarity())
                        .status(existing.getStatus())
                        .alreadyMarked(true)
                        .build())
                .orElseGet(() -> {
                    Attendance record = Attendance.builder()
                            .session(session)
                            .student(matchedStudent)
                            .status(AttendanceStatus.PRESENT)
                            .similarity(result.getSimilarity())
                            .matchedAt(Instant.now())
                            .build();
                    attendanceRepository.save(record);

                    return AttendanceMarkResponse.builder()
                            .matched(true)
                            .studentId(matchedStudent.getId())
                            .studentName(matchedStudent.getName())
                            .rollNo(matchedStudent.getRollNo())
                            .similarity(result.getSimilarity())
                            .status(record.getStatus())
                            .alreadyMarked(false)
                            .build();
                });
    }
}
