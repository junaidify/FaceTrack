package com.facetrack.service;

import com.facetrack.dto.schoolclass.ClassCreateRequest;
import com.facetrack.dto.schoolclass.ClassResponse;
import com.facetrack.dto.schoolclass.ClassUpdateRequest;
import com.facetrack.entity.SchoolClass;
import com.facetrack.entity.Student;
import com.facetrack.entity.User;
import com.facetrack.enums.UserRole;
import com.facetrack.exception.ConflictException;
import com.facetrack.exception.ForbiddenException;
import com.facetrack.exception.NotFoundException;
import com.facetrack.repository.SchoolClassRepository;
import com.facetrack.repository.StudentRepository;
import com.facetrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolClassService {

    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public List<ClassResponse> listClasses(User currentUser) {
        List<SchoolClass> classes;
        if (currentUser.getRole() == UserRole.ADMIN) {
            classes = classRepository.findAll();
        } else {
            classes = classRepository.findByTeacherId(currentUser.getId());
        }
        return classes.stream().map(ClassResponse::from).toList();
    }

    public ClassResponse getClass(UUID classId, User currentUser) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));
        if (currentUser.getRole() != UserRole.ADMIN
                && !sc.getTeacher().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not own this class.");
        }
        return ClassResponse.from(sc);
    }

    @Transactional
    public ClassResponse create(ClassCreateRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new NotFoundException("Teacher not found."));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new NotFoundException("Teacher not found.");
        }
        if (classRepository.existsByName(request.getName())) {
            throw new ConflictException("A class with this name already exists.");
        }

        SchoolClass sc = SchoolClass.builder()
                .name(request.getName())
                .teacher(teacher)
                .build();
        sc = classRepository.save(sc);
        return ClassResponse.from(sc);
    }

    @Transactional
    public ClassResponse update(UUID classId, ClassUpdateRequest request) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));

        if (request.getName() != null) {
            sc.setName(request.getName());
        }
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher not found."));
            if (teacher.getRole() != UserRole.TEACHER) {
                throw new NotFoundException("Teacher not found.");
            }
            sc.setTeacher(teacher);
        }
        sc = classRepository.save(sc);
        return ClassResponse.from(sc);
    }

    @Transactional
    public void delete(UUID classId) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));
        // Detach all students from this class
        studentRepository.detachAllFromClass(classId);
        classRepository.delete(sc);
    }

    @Transactional
    public ClassResponse addStudent(UUID classId, UUID studentId) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));

        student.setSchoolClass(sc);
        studentRepository.save(student);

        // Refresh to get updated students list
        sc = classRepository.findById(classId).orElseThrow();
        return ClassResponse.from(sc);
    }

    @Transactional
    public ClassResponse removeStudent(UUID classId, UUID studentId) {
        SchoolClass sc = classRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("Class not found."));

        studentRepository.findById(studentId).ifPresent(student -> {
            if (student.getSchoolClass() != null
                    && student.getSchoolClass().getId().equals(classId)) {
                student.setSchoolClass(null);
                studentRepository.save(student);
            }
        });

        sc = classRepository.findById(classId).orElseThrow();
        return ClassResponse.from(sc);
    }
}
