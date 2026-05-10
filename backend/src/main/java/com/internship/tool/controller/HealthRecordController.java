package com.internship.tool.controller;

import com.internship.tool.entity.HealthRecord;
import com.internship.tool.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Health Records", description = "CRUD + AI endpoints for health records")
public class HealthRecordController {

    private final HealthRecordService service;

    @GetMapping
    @Operation(summary = "Get all health records (paginated)")
    public ResponseEntity<Page<HealthRecord>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        return ResponseEntity.ok(service.findAll(PageRequest.of(page, size, Sort.by(sortBy).descending())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get health record by ID")
    public ResponseEntity<HealthRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new health record")
    public ResponseEntity<HealthRecord> create(@Valid @RequestBody HealthRecordRequest req) {
        HealthRecord record = mapToEntity(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(record));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a health record")
    public ResponseEntity<HealthRecord> update(@PathVariable Long id, @Valid @RequestBody HealthRecordRequest req) {
        return ResponseEntity.ok(service.update(id, mapToEntity(req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a health record")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search health records by title")
    public ResponseEntity<Page<HealthRecord>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.search(q, PageRequest.of(page, size)));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    @PostMapping("/{id}/ai/recommend")
    @Operation(summary = "Generate AI recommendations for a health record")
    public ResponseEntity<HealthRecord> recommend(@PathVariable Long id) {
        return ResponseEntity.ok(service.triggerAiRecommend(id));
    }

    @PostMapping("/{id}/ai/report")
    @Operation(summary = "Generate AI health report")
    public ResponseEntity<HealthRecord> report(@PathVariable Long id) {
        return ResponseEntity.ok(service.triggerAiReport(id));
    }

    @GetMapping("/export")
    @Operation(summary = "Export all health records as CSV")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=health_records.csv");
        List<HealthRecord> records = service.findAllForExport();
        try (PrintWriter writer = response.getWriter();
             CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                     "ID", "Title", "Age", "BMI", "BP Systolic", "BP Diastolic",
                     "Cholesterol", "Blood Sugar", "Exercise Hrs", "Sleep Hrs",
                     "Smoking", "Alcohol Units", "Stress Level", "Health Score", "Status", "Created At"))) {
            for (HealthRecord r : records) {
                csv.printRecord(r.getId(), r.getTitle(), r.getAge(), r.getBmi(),
                        r.getBloodPressureSystolic(), r.getBloodPressureDiastolic(),
                        r.getCholesterol(), r.getBloodSugar(), r.getExerciseHoursPerWeek(),
                        r.getSleepHoursPerDay(), r.getSmoking(), r.getAlcoholUnitsPerWeek(),
                        r.getStressLevel(), r.getHealthScore(), r.getStatus(), r.getCreatedAt());
            }
        }
    }

    private HealthRecord mapToEntity(HealthRecordRequest req) {
        return HealthRecord.builder()
                .title(req.getTitle())
                .age(req.getAge())
                .bmi(req.getBmi())
                .bloodPressureSystolic(req.getBloodPressureSystolic())
                .bloodPressureDiastolic(req.getBloodPressureDiastolic())
                .cholesterol(req.getCholesterol())
                .bloodSugar(req.getBloodSugar())
                .exerciseHoursPerWeek(req.getExerciseHoursPerWeek())
                .sleepHoursPerDay(req.getSleepHoursPerDay())
                .smoking(req.getSmoking())
                .alcoholUnitsPerWeek(req.getAlcoholUnitsPerWeek())
                .stressLevel(req.getStressLevel())
                .status("ACTIVE")
                .build();
    }

    @Data
    public static class HealthRecordRequest {
        @NotBlank @Size(max = 255)
        private String title;
        @NotNull @Min(0) @Max(150)
        private Integer age;
        @DecimalMin("0.0") @DecimalMax("100.0")
        private BigDecimal bmi;
        @Min(0) @Max(300)
        private Integer bloodPressureSystolic;
        @Min(0) @Max(200)
        private Integer bloodPressureDiastolic;
        @Min(0)
        private Integer cholesterol;
        @DecimalMin("0.0")
        private BigDecimal bloodSugar;
        @DecimalMin("0.0")
        private BigDecimal exerciseHoursPerWeek;
        @DecimalMin("0.0") @DecimalMax("24.0")
        private BigDecimal sleepHoursPerDay;
        private Boolean smoking = false;
        @Min(0)
        private Integer alcoholUnitsPerWeek;
        @Min(1) @Max(10)
        private Integer stressLevel;
    }
}
