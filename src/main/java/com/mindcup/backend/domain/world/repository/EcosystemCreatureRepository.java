package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.EcosystemCreature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EcosystemCreatureRepository extends JpaRepository<EcosystemCreature, Long> {
    List<EcosystemCreature> findAllByWorldLevelCodeAndUseYn(String worldLevelCode, String useYn);
    Optional<EcosystemCreature> findByCreatureCode(String creatureCode);
}
