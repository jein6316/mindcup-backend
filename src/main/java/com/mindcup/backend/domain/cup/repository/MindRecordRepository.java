package com.mindcup.backend.domain.cup.repository;

import com.mindcup.backend.domain.cup.entity.MindRecord;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;

@Repository
public interface MindRecordRepository extends JpaRepository<MindRecord, Long> {
    List<MindRecord> findAllByDailyCup(DailyCup dailyCup);

    @Query("SELECT r FROM MindRecord r JOIN r.dailyCup c WHERE c.userId = :userId AND c.recordDate BETWEEN :start AND :end")
    List<MindRecord> findAllRecordsInPeriod(Long userId, LocalDate start, LocalDate end);
}
