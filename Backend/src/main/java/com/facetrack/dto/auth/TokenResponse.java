package com.facetrack.dto.auth;

import com.facetrack.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "bearer";
    private UserRole role;
    private String name;
    private String email;
}
