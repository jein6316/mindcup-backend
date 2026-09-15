package com.mindcup.backend.domain.report.repository;

import com.mindcup.backend.domain.report.entity.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    
    Optional<WeeklyReport> findByUserIdAndStartDateAndEndDate(Long userId, LocalDate startDate, LocalDate endDate);
    
    Optional<WeeklyReport> findFirstByUserIdOrderByEndDateDesc(Long userId);
}
