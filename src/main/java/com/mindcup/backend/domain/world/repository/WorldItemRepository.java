package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.WorldItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorldItemRepository extends JpaRepository<WorldItem, Long> {
    List<WorldItem> findAllByWorldLevelCodeAndUseYn(String worldLevelCode, String useYn);
    Optional<WorldItem> findByItemCode(String itemCode);
}
