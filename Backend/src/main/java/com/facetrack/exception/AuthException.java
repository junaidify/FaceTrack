package com.facetrack.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends AppException {
    public AuthException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
