package com.mindcup.backend.domain.mission.repository;

import com.mindcup.backend.domain.mission.entity.DailyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DailyMissionRepository extends JpaRepository<DailyMission, Long> {
    List<DailyMission> findAllByUseYnOrderBySortOrderAsc(String useYn);
}
