package com.facetrack.controller;

import com.facetrack.dto.session.SessionCreateRequest;
import com.facetrack.dto.session.SessionResponse;
import com.facetrack.security.CurrentUser;
import com.facetrack.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public List<SessionResponse> listSessions(
            @RequestParam(name = "class_id", required = false) UUID classId) {
        return sessionService.list(classId, CurrentUser.get());
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable UUID sessionId) {
        return sessionService.get(sessionId, CurrentUser.get());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse startSession(@Valid @RequestBody SessionCreateRequest request) {
        return sessionService.start(request, CurrentUser.get());
    }

    @PostMapping("/{sessionId}/end")
    public SessionResponse endSession(@PathVariable UUID sessionId) {
        return sessionService.end(sessionId, CurrentUser.get());
    }
}
