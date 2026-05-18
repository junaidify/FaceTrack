package com.facetrack.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class StudentCreateRequest {

    @NotBlank
    @Size(min = 1, max = 40)
    private String rollNo;

    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    private UUID classId;
}
