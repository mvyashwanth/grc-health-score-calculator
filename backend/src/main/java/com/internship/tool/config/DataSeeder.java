package com.internship.tool.config;

import com.internship.tool.entity.HealthRecord;
import com.internship.tool.repository.HealthRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final HealthRecordRepository repository;

    @Override
    public void run(String... args) {
        if (repository.countByDeletedAtIsNull() >= 10) {
            log.info("Database already seeded, skipping.");
            return;
        }

        List<HealthRecord> records = List.of(
            seed("Alice Johnson", 34, 22.5, 118, 76, 185, 92.0, 4.0, 7.5, false, 3, 2),
            seed("Bob Martinez", 45, 27.8, 135, 88, 210, 105.0, 2.0, 6.0, true, 8, 7),
            seed("Carol Smith", 28, 19.2, 110, 70, 165, 85.0, 5.0, 8.0, false, 1, 3),
            seed("David Lee", 52, 31.4, 148, 94, 245, 120.0, 1.0, 5.5, true, 15, 9),
            seed("Emma Wilson", 39, 24.1, 122, 80, 190, 97.0, 3.5, 7.0, false, 4, 5),
            seed("Frank Brown", 61, 29.3, 142, 91, 228, 114.0, 1.5, 6.5, false, 10, 6),
            seed("Grace Taylor", 25, 20.8, 112, 72, 170, 88.0, 6.0, 8.5, false, 0, 2),
            seed("Henry Davis", 48, 33.2, 155, 98, 260, 130.0, 0.5, 5.0, true, 20, 10),
            seed("Iris Clark", 36, 23.7, 120, 78, 180, 94.0, 3.0, 7.5, false, 5, 4),
            seed("Jack Anderson", 42, 26.4, 130, 85, 205, 108.0, 2.5, 6.5, false, 7, 6),
            seed("Karen White", 31, 21.3, 116, 74, 172, 89.0, 4.5, 8.0, false, 2, 3),
            seed("Leo Harris", 55, 30.1, 145, 92, 235, 118.0, 1.0, 6.0, true, 12, 8),
            seed("Maya Robinson", 27, 18.9, 108, 68, 160, 83.0, 5.5, 8.5, false, 0, 2),
            seed("Nathan Lewis", 50, 28.6, 138, 89, 218, 111.0, 2.0, 6.0, false, 9, 7),
            seed("Olivia Walker", 33, 22.1, 117, 75, 182, 91.0, 4.0, 7.5, false, 3, 3),
            seed("Paul Hall", 58, 32.0, 150, 96, 250, 125.0, 0.5, 5.5, true, 18, 9),
            seed("Quinn Allen", 29, 20.5, 113, 73, 168, 87.0, 5.0, 8.0, false, 1, 2),
            seed("Rachel Young", 44, 25.9, 128, 83, 200, 103.0, 3.0, 7.0, false, 6, 5),
            seed("Sam King", 37, 24.5, 124, 81, 193, 98.0, 3.5, 7.0, false, 4, 4),
            seed("Tina Scott", 62, 27.2, 140, 90, 222, 115.0, 1.5, 6.5, false, 11, 7),
            seed("Uma Carter", 23, 18.5, 106, 67, 155, 80.0, 6.5, 9.0, false, 0, 1),
            seed("Victor Mitchell", 53, 34.1, 158, 100, 265, 135.0, 0.5, 5.0, true, 22, 10),
            seed("Wendy Perez", 40, 23.9, 121, 79, 186, 95.0, 3.5, 7.5, false, 4, 4),
            seed("Xander Roberts", 46, 29.8, 144, 93, 240, 120.0, 1.0, 6.0, true, 14, 8),
            seed("Yara Turner", 30, 21.7, 115, 74, 175, 90.0, 4.5, 8.0, false, 2, 3),
            seed("Zack Phillips", 56, 31.5, 148, 95, 248, 123.0, 1.0, 5.5, false, 13, 8),
            seed("Amber Campbell", 26, 19.8, 111, 71, 163, 86.0, 5.0, 8.5, false, 1, 2),
            seed("Brian Parker", 49, 27.5, 136, 87, 212, 109.0, 2.0, 6.5, false, 8, 6),
            seed("Cindy Evans", 35, 22.9, 119, 77, 188, 93.0, 4.0, 7.5, false, 3, 3),
            seed("Derek Edwards", 60, 33.5, 153, 97, 255, 128.0, 0.5, 5.0, true, 19, 9)
        );
        repository.saveAll(records);
        log.info("Seeded {} health records.", records.size());
    }

    private HealthRecord seed(String title, int age, double bmi, int sysP, int diaP,
                               int chol, double sugar, double exercise, double sleep,
                               boolean smoking, int alcohol, int stress) {
        double score = calculateScore(bmi, sysP, diaP, chol, sugar, exercise, sleep, smoking, alcohol, stress);
        String status = score >= 75 ? "EXCELLENT" : score >= 50 ? "GOOD" : score >= 30 ? "FAIR" : "POOR";
        return HealthRecord.builder()
                .title(title)
                .age(age)
                .bmi(BigDecimal.valueOf(bmi))
                .bloodPressureSystolic(sysP)
                .bloodPressureDiastolic(diaP)
                .cholesterol(chol)
                .bloodSugar(BigDecimal.valueOf(sugar))
                .exerciseHoursPerWeek(BigDecimal.valueOf(exercise))
                .sleepHoursPerDay(BigDecimal.valueOf(sleep))
                .smoking(smoking)
                .alcoholUnitsPerWeek(alcohol)
                .stressLevel(stress)
                .healthScore(BigDecimal.valueOf(score))
                .status(status)
                .aiDescription("AI analysis pending.")
                .build();
    }

    private double calculateScore(double bmi, int sysP, int diaP, int chol, double sugar,
                                   double exercise, double sleep, boolean smoking, int alcohol, int stress) {
        double score = 100.0;
        if (bmi < 18.5 || bmi > 30) score -= 15; else if (bmi > 25) score -= 7;
        if (sysP > 140) score -= 15;
        if (diaP > 90) score -= 10;
        if (chol > 200) score -= 10;
        if (sugar > 100) score -= 10;
        if (exercise >= 3) score += 5;
        if (sleep < 6 || sleep > 9) score -= 8;
        if (smoking) score -= 20;
        if (alcohol > 14) score -= 10;
        if (stress >= 8) score -= 10;
        return Math.max(0, Math.min(100, score));
    }
}
