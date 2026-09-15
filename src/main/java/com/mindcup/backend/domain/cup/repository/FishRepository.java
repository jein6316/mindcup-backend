package com.mindcup.backend.domain.cup.repository;

import com.mindcup.backend.domain.cup.entity.Fish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FishRepository extends JpaRepository<Fish, Long> {
    Optional<Fish> findByStatus(String status);
}
