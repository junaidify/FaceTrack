package com.facetrack.repository;

import com.facetrack.entity.AttendanceSession;
import com.facetrack.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    List<AttendanceSession> findBySchoolClassIdOrderByStartedAtDesc(UUID classId);

    List<AttendanceSession> findByTeacherIdOrderByStartedAtDesc(UUID teacherId);

    List<AttendanceSession> findBySchoolClassIdAndTeacherIdOrderByStartedAtDesc(UUID classId, UUID teacherId);

    Optional<AttendanceSession> findBySchoolClassIdAndStatus(UUID classId, SessionStatus status);

    List<AttendanceSession> findAllByOrderByStartedAtDesc();
}
