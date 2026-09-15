package com.mindcup.backend.domain.friend.repository;

import com.mindcup.backend.domain.friend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
           "WHERE ((f.userId = :userId AND f.targetUserId = :targetUserId) " +
           "OR (f.userId = :targetUserId AND f.targetUserId = :userId)) " +
           "AND f.status IN ('PENDING', 'ACCEPTED')")
    boolean existsPendingOrAcceptedRelation(Long userId, Long targetUserId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friend f " +
           "WHERE ((f.userId = :userId AND f.targetUserId = :targetUserId) " +
           "OR (f.userId = :targetUserId AND f.targetUserId = :userId)) " +
           "AND f.status = 'BLOCKED'")
    boolean existsBlockedRelation(Long userId, Long targetUserId);

    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId AND f.targetUserId = :targetUserId) " +
           "OR (f.userId = :targetUserId AND f.targetUserId = :userId)")
    Optional<Friend> findRelation(Long userId, Long targetUserId);

    @Query("SELECT f FROM Friend f WHERE (f.userId = :userId OR f.targetUserId = :userId) " +
           "AND f.status = 'ACCEPTED'")
    List<Friend> findAllAcceptedFriends(Long userId);

    List<Friend> findAllByTargetUserIdAndStatus(Long targetUserId, String status);
}
