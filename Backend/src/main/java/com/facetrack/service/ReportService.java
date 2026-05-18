package com.facetrack.service;

import com.facetrack.dto.attendance.SessionReportRow;
import com.facetrack.entity.*;
import com.facetrack.enums.AttendanceStatus;
import com.facetrack.enums.UserRole;
import com.facetrack.exception.ForbiddenException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.AttendanceRepository;
import com.facetrack.repository.AttendanceSessionRepository;
import com.facetrack.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExcelService excelService;

    private AttendanceSession getSessionAndAuthorize(UUID sessionId, User currentUser) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        if (currentUser.getRole() != UserRole.ADMIN
                && !session.getTeacher().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not own this session.");
        }
        return session;
    }

    public List<SessionReportRow> buildReport(UUID sessionId, User currentUser) {
        AttendanceSession session = getSessionAndAuthorize(sessionId, currentUser);
        return buildRows(session);
    }

    public ReportExcel buildExcelReport(UUID sessionId, User currentUser) {
        AttendanceSession session = getSessionAndAuthorize(sessionId, currentUser);
        SchoolClass sc = session.getSchoolClass();
        if (sc == null) {
            throw new NotFoundException("Class for this session no longer exists.");
        }

        List<SessionReportRow> rows = buildRows(session);
        String startedAt = session.getStartedAt().toString();
        byte[] blob = excelService.buildSessionWorkbook(sc.getName(), startedAt, rows);

        String filename = "attendance_" + sc.getName().replace(" ", "_") + "_"
                + session.getStartedAt().toString().replace(":", "").replace("-", "")
                + ".xlsx";

        return new ReportExcel(blob, filename);
    }

    private List<SessionReportRow> buildRows(AttendanceSession session) {
        SchoolClass sc = session.getSchoolClass();
        if (sc == null) {
            throw new NotFoundException("Class for this session no longer exists.");
        }

        List<Student> students = studentRepository.findBySchoolClassId(sc.getId());
        Map<UUID, Student> studentsById = students.stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        List<Attendance> records = attendanceRepository.findBySessionId(session.getId());
        Map<UUID, Attendance> recordsByStudent = records.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity()));

        List<SessionReportRow> rows = students.stream()
                .map(s -> {
                    Attendance rec = recordsByStudent.get(s.getId());
                    return SessionReportRow.builder()
                            .studentId(s.getId())
                            .rollNo(s.getRollNo())
                            .name(s.getName())
                            .status(rec != null ? rec.getStatus() : AttendanceStatus.ABSENT)
                            .similarity(rec != null ? rec.getSimilarity() : null)
                            .matchedAt(rec != null ? rec.getMatchedAt() : null)
                            .build();
                })
                .sorted(Comparator.comparing(SessionReportRow::getRollNo))
                .toList();

        return rows;
    }

    public record ReportExcel(byte[] data, String filename) {}
}
