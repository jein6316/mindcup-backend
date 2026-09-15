package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.UserCreature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserCreatureRepository extends JpaRepository<UserCreature, Long> {
    List<UserCreature> findAllByUserId(Long userId);
    boolean existsByUserIdAndCreatureId(Long userId, Long creatureId);
}
