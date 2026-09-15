package com.mindcup.backend.domain.mission.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindScoreEffectLog;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindScoreEffectLogRepository;
import com.mindcup.backend.domain.mission.dto.DailyMissionResponse;
import com.mindcup.backend.domain.mission.entity.DailyMission;
import com.mindcup.backend.domain.mission.entity.UserDailyMission;
import com.mindcup.backend.domain.mission.repository.DailyMissionRepository;
import com.mindcup.backend.domain.mission.repository.UserDailyMissionRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import com.mindcup.backend.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class DailyMissionServiceTest {

    @Autowired
    private DailyMissionService dailyMissionService;

    @Autowired
    private DailyMissionRepository dailyMissionRepository;

    @Autowired
    private UserDailyMissionRepository userDailyMissionRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private MindScoreEffectLogRepository effectLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    private User testUser;
    private DailyMission missionA;
    private DailyMission missionB;

    @BeforeEach
    void setUp() {
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
        }

        testUser = userRepository.save(User.builder().email("mission@mindcup.com").password("pass").nickname("MissionTester").provider("LOCAL").friendCode("MIS123").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());
        
        // 미션 마스터 시드 강제 적재
        dailyMissionRepository.deleteAll();
        missionA = dailyMissionRepository.save(new DailyMission("dm_test_a", "SELF_CARE", "mission.title.a", "mission.desc.a", -3, 1, "Y"));
        missionB = dailyMissionRepository.save(new DailyMission("dm_test_b", "REST", "mission.title.b", "mission.desc.b", -3, 2, "Y"));
    }

    @Test
    void testGetTodayMissionsCreatesDeterministicThreeMissions() {
        Long userId = testUser.getUserId();
        List<DailyMissionResponse> response = dailyMissionService.getTodayMissions(userId);
        
        // 마스터 미션이 2개이므로 최대 2개 생성 확인
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getMissionCode()).isEqualTo("dm_test_a");
        assertThat(response.get(0).getCompletedYn()).isFalse();
    }

    @Test
    void testCompleteMissionDecreasesScoreAndCreatesLog() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // 컵 데이터 기정
        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(50).activeWorldLevel("SMALL_CUP").build());

        // 오늘 미션 할당
        dailyMissionService.getTodayMissions(userId);

        // A 미션 완료
        dailyMissionService.completeMission(userId, missionA.getMissionId());

        // 점수 3점 감소 확인
        assertThat(cup.getPollutionScore()).isEqualTo(47);

        // 효과 로그가 DAILY_MISSION 타입으로 적재되었는지 검증
        int sum = effectLogRepository.sumEffectScoreByDateAndType(userId, today, "DAILY_MISSION");
        assertThat(sum).isEqualTo(-3);
    }

    @Test
    void testMissionLimitUpperBoundaryNotDecreased() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(50).activeWorldLevel("SMALL_CUP").build());
        dailyMissionService.getTodayMissions(userId);

        // 하루 미션 혜택 최대 누계인 -9점을 달성했다고 시뮬레이션하기 위해 가짜 로그 9점 차감 기입
        effectLogRepository.save(MindScoreEffectLog.builder()
                .userId(userId)
                .sourceType("DAILY_MISSION")
                .sourceId(999L)
                .effectScore(-9)
                .effectDate(today)
                .limitExceededYn("N")
                .build());

        // 혜택 가용한도가 0점이므로 완료해도 추가 컵 점수 차감 없이 50점 유지 검증
        dailyMissionService.completeMission(userId, missionA.getMissionId());
        assertThat(cup.getPollutionScore()).isEqualTo(50);
    }
}
