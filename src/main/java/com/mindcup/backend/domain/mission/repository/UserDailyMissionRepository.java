package com.mindcup.backend.domain.mission.repository;

import com.mindcup.backend.domain.mission.entity.UserDailyMission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserDailyMissionRepository extends JpaRepository<UserDailyMission, Long> {
    
    List<UserDailyMission> findAllByUserIdAndMissionDate(Long userId, LocalDate missionDate);
    
    Optional<UserDailyMission> findByUserIdAndMissionIdAndMissionDate(Long userId, Long missionId, LocalDate missionDate);
    
    long countByUserIdAndCompletedYn(Long userId, String completedYn);

    long countByUserIdAndCompletedYnAndMissionDateBetween(Long userId, String completedYn, LocalDate start, LocalDate end);
}
