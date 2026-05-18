package com.facetrack.controller;

import com.facetrack.dto.attendance.SessionReportRow;
import com.facetrack.security.CurrentUser;
import com.facetrack.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/sessions/{sessionId}")
    public List<SessionReportRow> sessionReport(@PathVariable UUID sessionId) {
        return reportService.buildReport(sessionId, CurrentUser.get());
    }

    @GetMapping("/sessions/{sessionId}/excel")
    public ResponseEntity<byte[]> sessionReportExcel(@PathVariable UUID sessionId) {
        ReportService.ReportExcel report = reportService.buildExcelReport(sessionId, CurrentUser.get());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + report.filename() + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(report.data());
    }
}
