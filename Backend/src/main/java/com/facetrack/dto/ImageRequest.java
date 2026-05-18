package com.facetrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ImageRequest {

    @NotBlank
    @Size(min = 32)
    private String image;
}
