package com.facetrack.dto.attendance;

import com.facetrack.enums.AttendanceStatus;
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
public class SessionReportRow {
    private UUID studentId;
    private String rollNo;
    private String name;
    private AttendanceStatus status;
    private Double similarity;
    private Instant matchedAt;
}
