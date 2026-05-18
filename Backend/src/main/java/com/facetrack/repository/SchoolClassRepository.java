package com.facetrack.repository;

import com.facetrack.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, UUID> {

    List<SchoolClass> findByTeacherId(UUID teacherId);

    Optional<SchoolClass> findByName(String name);

    boolean existsByName(String name);
}
