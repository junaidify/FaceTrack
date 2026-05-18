package com.facetrack.dto.attendance;

import com.facetrack.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMarkResponse {
    private boolean matched;
    private UUID studentId;
    private String studentName;
    private String rollNo;
    private Double similarity;
    private AttendanceStatus status;
    @Builder.Default
    private boolean alreadyMarked = false;
}
