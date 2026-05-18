package com.facetrack.repository;

import com.facetrack.entity.SchoolClass;
import com.facetrack.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    List<Student> findBySchoolClass(SchoolClass schoolClass);

    List<Student> findBySchoolClassId(UUID classId);

    Optional<Student> findBySchoolClassIdAndRollNo(UUID classId, String rollNo);

    @Query("SELECT s FROM Student s WHERE s.schoolClass.id = :classId AND s.faceEmbedding IS NOT NULL")
    List<Student> findEnrolledByClassId(@Param("classId") UUID classId);

    @Modifying
    @Query("UPDATE Student s SET s.schoolClass = null WHERE s.schoolClass.id = :classId")
    void detachAllFromClass(@Param("classId") UUID classId);
}
