package com.mindcup.backend.domain.user.repository;

import com.mindcup.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByFriendCode(String friendCode);
    Optional<User> findByFriendCode(String friendCode);
    Optional<User> findByRefreshToken(String refreshToken);
}
