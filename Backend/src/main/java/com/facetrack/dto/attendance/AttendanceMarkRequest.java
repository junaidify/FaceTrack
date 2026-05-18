package com.facetrack.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class AttendanceMarkRequest {

    @NotNull
    private UUID sessionId;

    @NotBlank
    @Size(min = 32)
    private String image;
}
