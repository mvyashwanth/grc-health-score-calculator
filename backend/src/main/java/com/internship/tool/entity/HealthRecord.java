package com.internship.tool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer age;

    @Column(precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    private Integer cholesterol;

    @Column(name = "blood_sugar", precision = 6, scale = 2)
    private BigDecimal bloodSugar;

    @Column(name = "exercise_hours_per_week", precision = 4, scale = 1)
    private BigDecimal exerciseHoursPerWeek;

    @Column(name = "sleep_hours_per_day", precision = 3, scale = 1)
    private BigDecimal sleepHoursPerDay;

    @Column(nullable = false)
    private Boolean smoking = false;

    @Column(name = "alcohol_units_per_week")
    private Integer alcoholUnitsPerWeek;

    @Column(name = "stress_level")
    private Integer stressLevel;

    @Column(name = "health_score", precision = 5, scale = 2)
    private BigDecimal healthScore;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "ai_description", columnDefinition = "TEXT")
    private String aiDescription;

    @Column(name = "ai_recommendations", columnDefinition = "TEXT")
    private String aiRecommendations;

    @Column(name = "ai_report", columnDefinition = "TEXT")
    private String aiReport;

    @Column(name = "is_fallback")
    @Builder.Default
    private Boolean isFallback = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
