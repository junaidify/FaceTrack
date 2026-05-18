package com.facetrack.repository;

import com.facetrack.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findBySessionId(UUID sessionId);

    Optional<Attendance> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    boolean existsBySessionIdAndStudentId(UUID sessionId, UUID studentId);
}
