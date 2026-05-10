package com.internship.tool.repository;

import com.internship.tool.entity.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    // Find all non-deleted records, paginated
    Page<HealthRecord> findByDeletedAtIsNull(Pageable pageable);

    // Find by ID and not deleted
    Optional<HealthRecord> findByIdAndDeletedAtIsNull(Long id);

    // Search by title (non-deleted)
    Page<HealthRecord> findByTitleContainingIgnoreCaseAndDeletedAtIsNull(String title, Pageable pageable);

    // Find by status (non-deleted)
    Page<HealthRecord> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);

    // Count by status for dashboard
    long countByStatusAndDeletedAtIsNull(String status);

    // Average health score
    @Query("SELECT AVG(h.healthScore) FROM HealthRecord h WHERE h.deletedAt IS NULL")
    Double findAverageHealthScore();

    // Count total non-deleted records
    long countByDeletedAtIsNull();

    // Find records for export
    List<HealthRecord> findByDeletedAtIsNull();

    // Find records by user
    Page<HealthRecord> findByUserIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
