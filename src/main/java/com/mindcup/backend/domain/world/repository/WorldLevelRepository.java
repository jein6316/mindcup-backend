package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.WorldLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WorldLevelRepository extends JpaRepository<WorldLevel, Long> {
    Optional<WorldLevel> findByWorldLevelCode(String worldLevelCode);
}
