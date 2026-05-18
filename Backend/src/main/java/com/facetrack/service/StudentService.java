package com.facetrack.service;

import com.facetrack.dto.student.StudentCreateRequest;
import com.facetrack.dto.student.StudentResponse;
import com.facetrack.dto.student.StudentUpdateRequest;
import com.facetrack.entity.SchoolClass;
import com.facetrack.entity.Student;
import com.facetrack.exception.ConflictException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.SchoolClassRepository;
import com.facetrack.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final FaceClientService faceClientService;

    public List<StudentResponse> list(UUID classId) {
        List<Student> students;
        if (classId != null) {
            students = studentRepository.findBySchoolClassId(classId);
        } else {
            students = studentRepository.findAll();
        }
        return students.stream().map(StudentResponse::from).toList();
    }

    public StudentResponse get(UUID studentId) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));
        return StudentResponse.from(s);
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        SchoolClass sc = null;
        if (request.getClassId() != null) {
            sc = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new NotFoundException("Class not found."));
            // Enforce unique roll_no within a class
            studentRepository.findBySchoolClassIdAndRollNo(request.getClassId(), request.getRollNo())
                    .ifPresent(existing -> {
                        throw new ConflictException("A student with this roll number already exists in the class.");
                    });
        }

        Student student = Student.builder()
                .rollNo(request.getRollNo())
                .name(request.getName())
                .schoolClass(sc)
                .build();
        student = studentRepository.save(student);

        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse update(UUID studentId, StudentUpdateRequest request) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));

        if (request.getRollNo() != null) {
            s.setRollNo(request.getRollNo());
        }
        if (request.getName() != null) {
            s.setName(request.getName());
        }
        if (request.getClassId() != null) {
            SchoolClass newClass = classRepository.findById(request.getClassId())
                    .orElseThrow(() -> new NotFoundException("Target class not found."));
            s.setSchoolClass(newClass);
        }

        s = studentRepository.save(s);
        return StudentResponse.from(s);
    }

    @Transactional
    public void delete(UUID studentId) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));
        studentRepository.delete(s);
    }

    @Transactional
    public StudentResponse enrollFace(UUID studentId, String imageB64) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));

        List<Double> embedding = faceClientService.embed(imageB64);
        s.setFaceEmbedding(embedding);
        s.setEnrolledAt(Instant.now());
        s = studentRepository.save(s);
        return StudentResponse.from(s);
    }

    @Transactional
    public StudentResponse clearFace(UUID studentId) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));
        s.setFaceEmbedding(null);
        s.setEnrolledAt(null);
        s = studentRepository.save(s);
        return StudentResponse.from(s);
    }
}
