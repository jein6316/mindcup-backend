package com.mindcup.backend.domain.world.repository;

import com.mindcup.backend.domain.world.entity.UserWorld;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserWorldRepository extends JpaRepository<UserWorld, Long> {
    Optional<UserWorld> findByUserId(Long userId);
}
