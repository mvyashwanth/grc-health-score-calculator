package com.internship.tool.service;

import com.internship.tool.config.AiServiceClient;
import com.internship.tool.entity.HealthRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthRecordService {

    private final HealthRecordRepository repository;
    private final AiServiceClient aiServiceClient;

    @Cacheable(value = "healthRecords", key = "'page:' + #pageable.pageNumber")
    public Page<HealthRecord> findAll(Pageable pageable) {
        return repository.findByDeletedAtIsNull(pageable);
    }

    @Cacheable(value = "healthRecord", key = "#id")
    public HealthRecord findById(Long id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthRecord", id));
    }

    @Transactional
    @CacheEvict(value = {"healthRecords", "healthStats"}, allEntries = true)
    public HealthRecord create(HealthRecord record) {
        record.setHealthScore(calculateScore(record));
        HealthRecord saved = repository.save(record);
        enrichWithAiAsync(saved);
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"healthRecords", "healthRecord", "healthStats"}, allEntries = true)
    public HealthRecord update(Long id, HealthRecord updated) {
        HealthRecord existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setAge(updated.getAge());
        existing.setBmi(updated.getBmi());
        existing.setBloodPressureSystolic(updated.getBloodPressureSystolic());
        existing.setBloodPressureDiastolic(updated.getBloodPressureDiastolic());
        existing.setCholesterol(updated.getCholesterol());
        existing.setBloodSugar(updated.getBloodSugar());
        existing.setExerciseHoursPerWeek(updated.getExerciseHoursPerWeek());
        existing.setSleepHoursPerDay(updated.getSleepHoursPerDay());
        existing.setSmoking(updated.getSmoking());
        existing.setAlcoholUnitsPerWeek(updated.getAlcoholUnitsPerWeek());
        existing.setStressLevel(updated.getStressLevel());
        existing.setStatus(updated.getStatus());
        existing.setHealthScore(calculateScore(existing));
        return repository.save(existing);
    }

    @Transactional
    @CacheEvict(value = {"healthRecords", "healthRecord", "healthStats"}, allEntries = true)
    public void softDelete(Long id) {
        HealthRecord record = findById(id);
        record.setDeletedAt(LocalDateTime.now());
        record.setStatus("DELETED");
        repository.save(record);
    }

    public Page<HealthRecord> search(String query, Pageable pageable) {
        return repository.findByTitleContainingIgnoreCaseAndDeletedAtIsNull(query, pageable);
    }

    @Cacheable(value = "healthStats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.countByDeletedAtIsNull());
        stats.put("active", repository.countByStatusAndDeletedAtIsNull("ACTIVE"));
        stats.put("avgScore", repository.findAverageHealthScore());
        return stats;
    }

    public List<HealthRecord> findAllForExport() {
        return repository.findByDeletedAtIsNull();
    }

    @Async
    public void enrichWithAiAsync(HealthRecord record) {
        try {
            Map<String, Object> data = buildAiPayload(record);
            String description = aiServiceClient.describe(data);
            if (description != null) {
                record.setAiDescription(description);
                record.setIsFallback(false);
            } else {
                record.setAiDescription("AI description temporarily unavailable.");
                record.setIsFallback(true);
            }
            repository.save(record);
        } catch (Exception e) {
            log.error("Async AI enrichment failed for record {}: {}", record.getId(), e.getMessage());
        }
    }

    public HealthRecord triggerAiRecommend(Long id) {
        HealthRecord record = findById(id);
        Map<String, Object> data = buildAiPayload(record);
        String recommendations = aiServiceClient.recommend(data);
        record.setAiRecommendations(recommendations != null ? recommendations : "Recommendations unavailable.");
        return repository.save(record);
    }

    public HealthRecord triggerAiReport(Long id) {
        HealthRecord record = findById(id);
        Map<String, Object> data = buildAiPayload(record);
        String report = aiServiceClient.generateReport(data);
        record.setAiReport(report != null ? report : "Report generation failed.");
        return repository.save(record);
    }

    private BigDecimal calculateScore(HealthRecord r) {
        double score = 100.0;

        // BMI scoring
        if (r.getBmi() != null) {
            double bmi = r.getBmi().doubleValue();
            if (bmi < 18.5 || bmi > 30) score -= 15;
            else if (bmi > 25) score -= 7;
        }

        // Blood pressure
        if (r.getBloodPressureSystolic() != null && r.getBloodPressureSystolic() > 140) score -= 15;
        if (r.getBloodPressureDiastolic() != null && r.getBloodPressureDiastolic() > 90) score -= 10;

        // Cholesterol
        if (r.getCholesterol() != null && r.getCholesterol() > 200) score -= 10;

        // Blood sugar
        if (r.getBloodSugar() != null && r.getBloodSugar().doubleValue() > 100) score -= 10;

        // Exercise (bonus)
        if (r.getExerciseHoursPerWeek() != null && r.getExerciseHoursPerWeek().doubleValue() >= 3) score += 5;

        // Sleep
        if (r.getSleepHoursPerDay() != null) {
            double sleep = r.getSleepHoursPerDay().doubleValue();
            if (sleep < 6 || sleep > 9) score -= 8;
        }

        // Smoking penalty
        if (Boolean.TRUE.equals(r.getSmoking())) score -= 20;

        // Alcohol
        if (r.getAlcoholUnitsPerWeek() != null && r.getAlcoholUnitsPerWeek() > 14) score -= 10;

        // Stress
        if (r.getStressLevel() != null && r.getStressLevel() >= 8) score -= 10;

        return BigDecimal.valueOf(Math.max(0, Math.min(100, score)));
    }

    private Map<String, Object> buildAiPayload(HealthRecord r) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", r.getTitle());
        data.put("age", r.getAge());
        data.put("bmi", r.getBmi());
        data.put("blood_pressure_systolic", r.getBloodPressureSystolic());
        data.put("blood_pressure_diastolic", r.getBloodPressureDiastolic());
        data.put("cholesterol", r.getCholesterol());
        data.put("blood_sugar", r.getBloodSugar());
        data.put("exercise_hours_per_week", r.getExerciseHoursPerWeek());
        data.put("sleep_hours_per_day", r.getSleepHoursPerDay());
        data.put("smoking", r.getSmoking());
        data.put("alcohol_units_per_week", r.getAlcoholUnitsPerWeek());
        data.put("stress_level", r.getStressLevel());
        data.put("health_score", r.getHealthScore());
        return data;
    }
}
