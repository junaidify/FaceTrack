package com.facetrack.dto.schoolclass;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ClassCreateRequest {

    @NotBlank
    @Size(min = 1, max = 120)
    private String name;

    @NotNull
    private UUID teacherId;
}
