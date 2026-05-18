package com.facetrack.controller;

import com.facetrack.dto.ImageRequest;
import com.facetrack.dto.student.StudentCreateRequest;
import com.facetrack.dto.student.StudentResponse;
import com.facetrack.dto.student.StudentUpdateRequest;
import com.facetrack.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public List<StudentResponse> listStudents(
            @RequestParam(name = "class_id", required = false) UUID classId) {
        return studentService.list(classId);
    }

    @GetMapping("/{studentId}")
    public StudentResponse getStudent(@PathVariable UUID studentId) {
        return studentService.get(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse createStudent(@Valid @RequestBody StudentCreateRequest request) {
        return studentService.create(request);
    }

    @PatchMapping("/{studentId}")
    public StudentResponse updateStudent(@PathVariable UUID studentId,
                                         @Valid @RequestBody StudentUpdateRequest request) {
        return studentService.update(studentId, request);
    }

    @DeleteMapping("/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStudent(@PathVariable UUID studentId) {
        studentService.delete(studentId);
    }

    @PostMapping("/{studentId}/enroll-face")
    public StudentResponse enrollFace(@PathVariable UUID studentId,
                                      @Valid @RequestBody ImageRequest request) {
        return studentService.enrollFace(studentId, request.getImage());
    }

    @DeleteMapping("/{studentId}/enroll-face")
    public StudentResponse clearFace(@PathVariable UUID studentId) {
        return studentService.clearFace(studentId);
    }
}
