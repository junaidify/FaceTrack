package com.facetrack.controller;

import com.facetrack.dto.attendance.AttendanceMarkRequest;
import com.facetrack.dto.attendance.AttendanceMarkResponse;
import com.facetrack.security.CurrentUser;
import com.facetrack.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/mark")
    public AttendanceMarkResponse mark(@Valid @RequestBody AttendanceMarkRequest request) {
        return attendanceService.mark(request, CurrentUser.get());
    }
}
