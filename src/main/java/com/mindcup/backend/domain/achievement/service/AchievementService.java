package com.mindcup.backend.domain.achievement.service;

import com.mindcup.backend.domain.achievement.dto.AchievementResponse;
import com.mindcup.backend.domain.achievement.entity.Achievement;
import com.mindcup.backend.domain.achievement.entity.TriggerType;
import com.mindcup.backend.domain.achievement.entity.UserAchievement;
import com.mindcup.backend.domain.achievement.repository.AchievementRepository;
import com.mindcup.backend.domain.achievement.repository.UserAchievementRepository;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageRepository;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindScoreEffectLogRepository;
import com.mindcup.backend.domain.mission.repository.UserDailyMissionRepository;
import com.mindcup.backend.domain.world.entity.UserWorld;
import com.mindcup.backend.domain.world.repository.UserWorldRepository;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final DailyCupRepository dailyCupRepository;
    private final ComfortMessageRepository comfortMessageRepository;
    private final UserWorldRepository userWorldRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final UserDailyMissionRepository userDailyMissionRepository;
    private final MindScoreEffectLogRepository effectLogRepository;
    private final UserRepository userRepository;

    /**
     * 전체 업적 리스트 조회 (본인 달성 여부 매핑)
     */
    public List<AchievementResponse> getAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        List<Achievement> achievements = achievementRepository.findAllByUseYnOrderBySortOrderAsc("Y");
        Set<Long> myAcquiredIds = userAchievementRepository.findAllByUserId(userId).stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        List<AchievementResponse> responseList = new ArrayList<>();
        for (Achievement ach : achievements) {
            boolean achieved = myAcquiredIds.contains(ach.getAchievementId());
            responseList.add(AchievementResponse.builder()
                    .achievementId(ach.getAchievementId())
                    .achievementCode(ach.getAchievementCode())
                    .title(ach.getTitleKey())
                    .description(ach.getDescriptionKey())
                    .achievedYn(achieved)
                    .achievedAt(achieved ? LocalDateTimeDummy() : null) // 실 획득 시점 매핑용
                    .build());
        }

        return responseList;
    }

    private java.time.LocalDateTime LocalDateTimeDummy() {
        return java.time.LocalDateTime.now();
    }

    /**
     * 업적 조건 평가 트리거 서비스 (중복 획득 자동 방어, E2E 단일 트랜잭션 내 가동)
     */
    @Transactional
    public void evaluate(Long userId, TriggerType triggerType) {
        List<Achievement> achievements = achievementRepository.findAllByUseYnOrderBySortOrderAsc("Y");

        for (Achievement ach : achievements) {
            // 이미 획득한 업적은 중복 평가 배제
            if (userAchievementRepository.existsByUserIdAndAchievementId(userId, ach.getAchievementId())) {
                continue;
            }

            boolean conditionMet = false;
            String condType = ach.getConditionType();
            int condVal = ach.getConditionValue();

            switch (condType) {
                case "CLEAR_ACTION_COUNT":
                    // 맑음 행동 기록 수 (탁도가 50 미만으로 떨어진 기록일 수)
                    long clearCount = dailyCupRepository.findAll().stream()
                            .filter(c -> c.getUserId().equals(userId) && c.getPollutionScore() < 50)
                            .count();
                    conditionMet = (clearCount >= condVal);
                    break;

                case "CLOUDY_ACTION_COUNT":
                    // 흐림 기록 수 (탁도가 50 초과인 날 수)
                    long cloudyCount = dailyCupRepository.findAll().stream()
                            .filter(c -> c.getUserId().equals(userId) && c.getPollutionScore() > 50)
                            .count();
                    conditionMet = (cloudyCount >= condVal);
                    break;

                case "COMFORT_SENT_COUNT":
                    // 한 방울 보낸 횟수
                    long sentCount = comfortMessageRepository.findAll().stream()
                            .filter(m -> m.getSenderUserId().equals(userId))
                            .count();
                    conditionMet = (sentCount >= condVal);
                    break;

                case "COMFORT_POURED_COUNT":
                    // 받은 한 방울 부은 횟수
                    long pouredCount = comfortMessageRepository.findAll().stream()
                            .filter(m -> m.getReceiverUserId().equals(userId) && "Y".equals(m.getPouredYn()))
                            .count();
                    conditionMet = (pouredCount >= condVal);
                    break;

                case "WORLD_UNLOCKED":
                    // 해금된 세계 성장 레벨 기준
                    UserWorld uw = userWorldRepository.findByUserId(userId).orElse(null);
                    if (uw != null) {
                        String unlockedLevel = uw.getUnlockedWorldLevel();
                        // 월드 레벨 마스터 순서(sortOrder)를 가져와 비교
                        int currentSortOrder = worldLevelRepository.findByWorldLevelCode(unlockedLevel)
                                .map(com.mindcup.backend.domain.world.entity.WorldLevel::getSortOrder).orElse(1);
                        conditionMet = (currentSortOrder >= condVal);
                    }
                    break;

                case "DAILY_MISSION_COUNT":
                    // 완료한 일일 미션 총수
                    long missionCount = userDailyMissionRepository.countByUserIdAndCompletedYn(userId, "Y");
                    conditionMet = (missionCount >= condVal);
                    break;

                case "POLLUTION_DECREASE_IN_DAY":
                    // 하루 누적 탁도 감소량 (abs(sum(effectScore)) 가 condVal 이상인 날이 당일에 존재하는가)
                    int sumToday = effectLogRepository.sumEffectScoreByDateAndType(userId, LocalDate.now(), "DAILY_MISSION")
                            + effectLogRepository.sumEffectScoreByDateAndType(userId, LocalDate.now(), "COMFORT_SENT")
                            + effectLogRepository.sumEffectScoreByDateAndType(userId, LocalDate.now(), "COMFORT_POURED");
                    // 맑음/미션 등으로 음수 차감이 누적되므로 절대값 환산
                    conditionMet = (Math.abs(sumToday) >= condVal);
                    break;

                case "RECORD_IN_VERY_CLOUDY":
                    // VERY_CLOUDY(71점 이상) 상태에서 한 번이라도 일상 기록을 기록한 적이 있는가
                    boolean recordedInCloudy = dailyCupRepository.findAll().stream()
                            .anyMatch(c -> c.getUserId().equals(userId) && c.getPollutionScore() >= 71);
                    conditionMet = recordedInCloudy;
                    break;
            }

            // 조건 충족 시 업적 자동 획득
            if (conditionMet) {
                userAchievementRepository.save(UserAchievement.builder()
                        .userId(userId)
                        .achievementId(ach.getAchievementId())
                        .build());
                log.info("사용자 {} 가 업적 {} 을 달성했습니다!", userId, ach.getAchievementCode());
            }
        }
    }
}
