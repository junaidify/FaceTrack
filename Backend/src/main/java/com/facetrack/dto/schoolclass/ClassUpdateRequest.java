package com.facetrack.dto.schoolclass;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClassUpdateRequest {

    @Size(min = 1, max = 120)
    private String name;

    private UUID teacherId;
}
