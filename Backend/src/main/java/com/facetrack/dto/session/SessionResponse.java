package com.facetrack.dto.session;

import com.facetrack.entity.AttendanceSession;
import com.facetrack.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private UUID id;
    private UUID classId;
    private UUID teacherId;
    private SessionStatus status;
    private Instant startedAt;
    private Instant endedAt;
    private String note;

    public static SessionResponse from(AttendanceSession s) {
        return SessionResponse.builder()
                .id(s.getId())
                .classId(s.getSchoolClass().getId())
                .teacherId(s.getTeacher().getId())
                .status(s.getStatus())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .note(s.getNote())
                .build();
    }
}
