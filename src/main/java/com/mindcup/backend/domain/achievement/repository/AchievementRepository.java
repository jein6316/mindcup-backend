package com.mindcup.backend.domain.achievement.repository;

import com.mindcup.backend.domain.achievement.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findAllByUseYnOrderBySortOrderAsc(String useYn);
}
