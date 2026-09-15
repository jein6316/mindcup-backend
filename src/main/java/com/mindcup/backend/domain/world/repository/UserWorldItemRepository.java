package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.UserWorldItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserWorldItemRepository extends JpaRepository<UserWorldItem, Long> {
    List<UserWorldItem> findAllByUserId(Long userId);
    List<UserWorldItem> findAllByUserIdAndEquippedYn(Long userId, String equippedYn);
    boolean existsByUserIdAndItemId(Long userId, Long itemId);
    Optional<UserWorldItem> findByUserIdAndItemId(Long userId, Long itemId);
}
