package com.facetrack.dto.session;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SessionCreateRequest {

    @NotNull
    private UUID classId;

    @Size(max = 500)
    private String note;
}
