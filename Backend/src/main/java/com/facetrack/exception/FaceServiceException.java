package com.facetrack.exception;

import org.springframework.http.HttpStatus;

public class FaceServiceException extends AppException {
    public FaceServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY);
    }

    public FaceServiceException(String message, HttpStatus status) {
        super(message, status);
    }
}
