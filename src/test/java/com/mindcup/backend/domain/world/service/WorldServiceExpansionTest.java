package com.mindcup.backend.domain.world.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.world.entity.UserWorld;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.UserWorldRepository;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class WorldServiceExpansionTest {

    @Autowired
    private WorldService worldService;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWorldRepository userWorldRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 4단계 확장용 마스터 레벨 시드 수동 장착
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
            worldLevelRepository.save(new WorldLevel("LARGE_CUP", "world.largeCup", 30, 2, "Y"));
            worldLevelRepository.save(new WorldLevel("AQUARIUM", "world.aquarium", 45, 3, "Y"));
            worldLevelRepository.save(new WorldLevel("POND", "world.pond", 60, 4, "Y"));
            worldLevelRepository.save(new WorldLevel("STREAM", "world.stream", 70, 5, "Y"));
            worldLevelRepository.save(new WorldLevel("RIVER", "world.river", 80, 6, "Y"));
            worldLevelRepository.save(new WorldLevel("LAKE", "world.lake", 88, 7, "Y"));
            worldLevelRepository.save(new WorldLevel("SEA", "world.sea", 92, 8, "Y"));
        }

        testUser = userRepository.save(User.builder().email("expansion@mindcup.com").password("pass").nickname("ExpTester").provider("LOCAL").friendCode("EXP123").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());
    }

    @Test
    void testExpansionThresholdsAndPermanentUnlock() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // 1. 평균 맑음도 72점 (STREAM 해금 임계치 70 이상)
        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(28).activeWorldLevel("SMALL_CUP").build());
        UserWorld uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("STREAM");

        // 2. 평균 맑음도 82점 (RIVER 해금 임계치 80 이상)
        cup.updatePollutionScore(18);
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("RIVER");

        // 3. 평균 맑음도 89점 (LAKE 해금 임계치 88 이상)
        cup.updatePollutionScore(11);
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("LAKE");

        // 4. 평균 맑음도 95점 (SEA 해금 임계치 92 이상)
        cup.updatePollutionScore(5);
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("SEA");

        // 5. 영구 해금 정책 검증: 이후 점수가 10점으로 급락하더라도 최고Unlocked는 여전히 SEA로 고정 보존되는지 검증
        cup.updatePollutionScore(90);
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("SEA");
        assertThat(uw.getCalculatedWorldLevel()).isEqualTo("SMALL_CUP"); // 실시간 도달가능은 축소
    }
}
