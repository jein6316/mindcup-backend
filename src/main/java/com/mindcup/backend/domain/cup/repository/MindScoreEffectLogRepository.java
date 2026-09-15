package com.mindcup.backend.domain.cup.repository;

import com.mindcup.backend.domain.cup.entity.MindScoreEffectLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;

public interface MindScoreEffectLogRepository extends JpaRepository<MindScoreEffectLog, Long> {
    
    @Query("SELECT COALESCE(SUM(l.effectScore), 0) FROM MindScoreEffectLog l " +
           "WHERE l.userId = :userId " +
           "AND l.effectDate = :effectDate " +
           "AND l.sourceType = :sourceType " +
           "AND l.limitExceededYn = 'N'")
    int sumEffectScoreByDateAndType(Long userId, LocalDate effectDate, String sourceType);
}
