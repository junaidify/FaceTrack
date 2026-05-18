package com.facetrack.dto.student;

import com.facetrack.entity.Student;
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
public class StudentResponse {
    private UUID id;
    private String rollNo;
    private String name;
    private UUID classId;
    private boolean enrolled;
    private Instant enrolledAt;
    private Instant createdAt;

    public static StudentResponse from(Student s) {
        return StudentResponse.builder()
                .id(s.getId())
                .rollNo(s.getRollNo())
                .name(s.getName())
                .classId(s.getSchoolClass() != null ? s.getSchoolClass().getId() : null)
                .enrolled(s.isEnrolled())
                .enrolledAt(s.getEnrolledAt())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
