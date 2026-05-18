package com.facetrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_class_roll", columnList = "class_id, roll_no", unique = true),
        @Index(name = "idx_student_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "roll_no", nullable = false, length = 40)
    private String rollNo;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    /**
     * 512-dimensional ArcFace embedding stored as a JSON array in PostgreSQL.
     * Uses jsonb column type for efficient storage and potential future similarity queries.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "face_embedding", columnDefinition = "jsonb")
    private List<Double> faceEmbedding;

    @Column(name = "enrolled_at")
    private Instant enrolledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    public boolean isEnrolled() {
        return faceEmbedding != null && !faceEmbedding.isEmpty();
    }
}
