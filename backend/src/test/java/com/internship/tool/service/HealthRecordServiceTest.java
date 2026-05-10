package com.internship.tool.service;

import com.internship.tool.config.AiServiceClient;
import com.internship.tool.entity.HealthRecord;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.HealthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthRecordServiceTest {

    @Mock private HealthRecordRepository repository;
    @Mock private AiServiceClient aiServiceClient;
    @InjectMocks private HealthRecordService service;

    private HealthRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = HealthRecord.builder()
                .id(1L)
                .title("Test Patient")
                .age(35)
                .bmi(BigDecimal.valueOf(24.5))
                .bloodPressureSystolic(120)
                .bloodPressureDiastolic(80)
                .cholesterol(190)
                .bloodSugar(BigDecimal.valueOf(95))
                .exerciseHoursPerWeek(BigDecimal.valueOf(3.0))
                .sleepHoursPerDay(BigDecimal.valueOf(7.5))
                .smoking(false)
                .alcoholUnitsPerWeek(4)
                .stressLevel(4)
                .status("ACTIVE")
                .build();
    }

    @Test
    void findById_existingRecord_returnsRecord() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleRecord));
        HealthRecord result = service.findById(1L);
        assertNotNull(result);
        assertEquals("Test Patient", result.getTitle());
    }

    @Test
    void findById_missingRecord_throwsNotFoundException() {
        when(repository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void create_calculatesHealthScore() {
        when(repository.save(any())).thenReturn(sampleRecord);
        HealthRecord toCreate = HealthRecord.builder()
                .title("New Patient").age(30)
                .bmi(BigDecimal.valueOf(22.0))
                .bloodPressureSystolic(118).bloodPressureDiastolic(76)
                .cholesterol(180).bloodSugar(BigDecimal.valueOf(88))
                .exerciseHoursPerWeek(BigDecimal.valueOf(4.0))
                .sleepHoursPerDay(BigDecimal.valueOf(8.0))
                .smoking(false).alcoholUnitsPerWeek(2).stressLevel(3)
                .build();
        service.create(toCreate);
        verify(repository, times(1)).save(any());
        assertNotNull(toCreate.getHealthScore());
        assertTrue(toCreate.getHealthScore().doubleValue() > 0);
    }

    @Test
    void create_smoker_reducesScore() {
        HealthRecord smoker = HealthRecord.builder()
                .title("Smoker").age(40)
                .bmi(BigDecimal.valueOf(24.0))
                .bloodPressureSystolic(120).bloodPressureDiastolic(80)
                .smoking(true).stressLevel(5)
                .build();
        HealthRecord nonSmoker = HealthRecord.builder()
                .title("NonSmoker").age(40)
                .bmi(BigDecimal.valueOf(24.0))
                .bloodPressureSystolic(120).bloodPressureDiastolic(80)
                .smoking(false).stressLevel(5)
                .build();
        when(repository.save(any())).thenReturn(smoker);
        service.create(smoker);
        service.create(nonSmoker);
        assertTrue(smoker.getHealthScore().compareTo(nonSmoker.getHealthScore()) < 0);
    }

    @Test
    void softDelete_setsDeletedAt() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleRecord));
        when(repository.save(any())).thenReturn(sampleRecord);
        service.softDelete(1L);
        assertNotNull(sampleRecord.getDeletedAt());
        assertEquals("DELETED", sampleRecord.getStatus());
    }

    @Test
    void update_existingRecord_updatesFields() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleRecord));
        when(repository.save(any())).thenReturn(sampleRecord);
        HealthRecord updated = HealthRecord.builder()
                .title("Updated Name").age(36)
                .bmi(BigDecimal.valueOf(25.0))
                .bloodPressureSystolic(122).bloodPressureDiastolic(82)
                .smoking(false).stressLevel(5).status("ACTIVE")
                .build();
        HealthRecord result = service.update(1L, updated);
        verify(repository).save(any());
    }

    @Test
    void create_highBP_reducesScore() {
        HealthRecord highBP = HealthRecord.builder()
                .title("High BP").age(50)
                .bloodPressureSystolic(155).bloodPressureDiastolic(98)
                .smoking(false).stressLevel(5)
                .bmi(BigDecimal.valueOf(24.0))
                .build();
        when(repository.save(any())).thenReturn(highBP);
        service.create(highBP);
        assertTrue(highBP.getHealthScore().doubleValue() < 80);
    }

    @Test
    void create_goodMetrics_highScore() {
        HealthRecord healthy = HealthRecord.builder()
                .title("Healthy Person").age(25)
                .bmi(BigDecimal.valueOf(21.0))
                .bloodPressureSystolic(110).bloodPressureDiastolic(70)
                .cholesterol(170).bloodSugar(BigDecimal.valueOf(85))
                .exerciseHoursPerWeek(BigDecimal.valueOf(5.0))
                .sleepHoursPerDay(BigDecimal.valueOf(8.0))
                .smoking(false).alcoholUnitsPerWeek(2).stressLevel(2)
                .build();
        when(repository.save(any())).thenReturn(healthy);
        service.create(healthy);
        assertTrue(healthy.getHealthScore().doubleValue() >= 90);
    }

    @Test
    void getStats_returnsMap() {
        when(repository.countByDeletedAtIsNull()).thenReturn(30L);
        when(repository.countByStatusAndDeletedAtIsNull("ACTIVE")).thenReturn(20L);
        when(repository.findAverageHealthScore()).thenReturn(72.5);
        var stats = service.getStats();
        assertEquals(30L, stats.get("total"));
        assertEquals(20L, stats.get("active"));
        assertEquals(72.5, stats.get("avgScore"));
    }

    @Test
    void triggerAiRecommend_nullResponse_setsUnavailableMessage() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleRecord));
        when(aiServiceClient.recommend(any())).thenReturn(null);
        when(repository.save(any())).thenReturn(sampleRecord);
        HealthRecord result = service.triggerAiRecommend(1L);
        assertTrue(sampleRecord.getAiRecommendations().contains("unavailable"));
    }

    @Test
    void triggerAiReport_withResponse_setsReport() {
        when(repository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(sampleRecord));
        when(aiServiceClient.generateReport(any())).thenReturn("{\"title\":\"Test Report\"}");
        when(repository.save(any())).thenReturn(sampleRecord);
        service.triggerAiReport(1L);
        assertNotNull(sampleRecord.getAiReport());
    }
}
