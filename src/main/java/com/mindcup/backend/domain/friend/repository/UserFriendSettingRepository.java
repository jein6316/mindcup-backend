package com.mindcup.backend.domain.friend.repository;

import com.mindcup.backend.domain.friend.entity.UserFriendSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserFriendSettingRepository extends JpaRepository<UserFriendSetting, Long> {
    Optional<UserFriendSetting> findByUserId(Long userId);
}
