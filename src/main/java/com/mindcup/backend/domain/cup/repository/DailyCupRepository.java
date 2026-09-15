package com.mindcup.backend.domain.cup.repository;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyCupRepository extends JpaRepository<DailyCup, Long> {
    Optional<DailyCup> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);
    Optional<DailyCup> findFirstByUserIdAndRecordDateBeforeOrderByRecordDateDesc(Long userId, LocalDate recordDate);
    List<DailyCup> findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(Long userId, LocalDate start, LocalDate end);
}
