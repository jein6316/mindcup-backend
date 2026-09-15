package com.mindcup.backend.domain.achievement.service;

import com.mindcup.backend.domain.achievement.dto.AchievementResponse;
import com.mindcup.backend.domain.achievement.entity.Achievement;
import com.mindcup.backend.domain.achievement.entity.TriggerType;
import com.mindcup.backend.domain.achievement.repository.AchievementRepository;
import com.mindcup.backend.domain.achievement.repository.UserAchievementRepository;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AchievementServiceTest {

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    private User testUser;
    private Achievement achClear;

    @BeforeEach
    void setUp() {
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
        }

        testUser = userRepository.save(User.builder().email("ach@mindcup.com").password("pass").nickname("AchTester").provider("LOCAL").friendCode("ACH999").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());

        // 첫 맑음기록 1회 달성 업적 마스터 강제 적재
        achievementRepository.deleteAll();
        achClear = achievementRepository.save(new Achievement("ach_test_clear", "ach.title", "ach.desc", "CLEAR_ACTION_COUNT", 1, null, null, 1, "Y"));
    }

    @Test
    void testEvaluateAutomaticallyGrantsAchievement() {
        Long userId = testUser.getUserId();

        // 1. 아직 조건 미달성 상태
        achievementService.evaluate(userId, TriggerType.CLEAR_ACTION_CREATED);
        boolean isAcquired = userAchievementRepository.existsByUserIdAndAchievementId(userId, achClear.getAchievementId());
        assertThat(isAcquired).isFalse();

        // 2. 오늘 자 맑음 점수 기록 등록 (10점 -> CLEAR, 50 미만이므로 맑음행동 요건 충족)
        dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(LocalDate.now()).pollutionScore(10).activeWorldLevel("SMALL_CUP").build());

        // 트리거 평가
        achievementService.evaluate(userId, TriggerType.CLEAR_ACTION_CREATED);

        // 획득 성공 검증
        isAcquired = userAchievementRepository.existsByUserIdAndAchievementId(userId, achClear.getAchievementId());
        assertThat(isAcquired).isTrue();
    }
}
