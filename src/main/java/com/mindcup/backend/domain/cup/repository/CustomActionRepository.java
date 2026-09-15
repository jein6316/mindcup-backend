package com.mindcup.backend.domain.cup.repository;

import com.mindcup.backend.domain.cup.entity.CustomAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomActionRepository extends JpaRepository<CustomAction, Long> {
    List<CustomAction> findAllByUserId(Long userId);
    Optional<CustomAction> findByUserIdAndActionNameAndActionType(Long userId, String actionName, String actionType);
}
