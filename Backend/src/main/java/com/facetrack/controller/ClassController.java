package com.facetrack.controller;

import com.facetrack.dto.schoolclass.ClassCreateRequest;
import com.facetrack.dto.schoolclass.ClassResponse;
import com.facetrack.dto.schoolclass.ClassUpdateRequest;
import com.facetrack.security.CurrentUser;
import com.facetrack.service.SchoolClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes")
@RequiredArgsConstructor
public class ClassController {

    private final SchoolClassService classService;

    @GetMapping
    public List<ClassResponse> listClasses() {
        return classService.listClasses(CurrentUser.get());
    }

    @GetMapping("/{classId}")
    public ClassResponse getClass(@PathVariable UUID classId) {
        return classService.getClass(classId, CurrentUser.get());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ClassResponse createClass(@Valid @RequestBody ClassCreateRequest request) {
        return classService.create(request);
    }

    @PatchMapping("/{classId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClassResponse updateClass(@PathVariable UUID classId,
                                     @Valid @RequestBody ClassUpdateRequest request) {
        return classService.update(classId, request);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteClass(@PathVariable UUID classId) {
        classService.delete(classId);
    }

    @PostMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClassResponse addStudent(@PathVariable UUID classId, @PathVariable UUID studentId) {
        return classService.addStudent(classId, studentId);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClassResponse removeStudent(@PathVariable UUID classId, @PathVariable UUID studentId) {
        return classService.removeStudent(classId, studentId);
    }
}
