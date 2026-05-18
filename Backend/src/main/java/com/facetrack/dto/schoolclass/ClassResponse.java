package com.facetrack.dto.schoolclass;

import com.facetrack.entity.SchoolClass;
import com.facetrack.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassResponse {
    private UUID id;
    private String name;
    private UUID teacherId;
    private List<UUID> studentIds;
    private Instant createdAt;

    public static ClassResponse from(SchoolClass sc) {
        List<UUID> studentIds = sc.getStudents() != null
                ? sc.getStudents().stream().map(Student::getId).toList()
                : List.of();
        return ClassResponse.builder()
                .id(sc.getId())
                .name(sc.getName())
                .teacherId(sc.getTeacher().getId())
                .studentIds(studentIds)
                .createdAt(sc.getCreatedAt())
                .build();
    }
}
