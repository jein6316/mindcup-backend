package com.mindcup.backend.domain.mission.service;

import com.mindcup.backend.domain.achievement.entity.TriggerType;
import com.mindcup.backend.domain.achievement.service.AchievementService;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindScoreEffectLog;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindScoreEffectLogRepository;
import com.mindcup.backend.domain.mission.dto.DailyMissionResponse;
import com.mindcup.backend.domain.mission.entity.DailyMission;
import com.mindcup.backend.domain.mission.entity.UserDailyMission;
import com.mindcup.backend.domain.mission.repository.DailyMissionRepository;
import com.mindcup.backend.domain.mission.repository.UserDailyMissionRepository;
import com.mindcup.backend.domain.world.service.WorldService;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyMissionService {

    private final DailyMissionRepository dailyMissionRepository;
    private final UserDailyMissionRepository userDailyMissionRepository;
    private final DailyCupRepository dailyCupRepository;
    private final MindScoreEffectLogRepository effectLogRepository;
    private final UserRepository userRepository;
    private final WorldService worldService;
    
    @Lazy
    private final AchievementService achievementService;

    /**
     * 오늘의 deterministic 미션 3종 반환 및 자동 할당
     */
    @Transactional
    public List<DailyMissionResponse> getTodayMissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        String lang = user.getLanguageSetting();
        LocalDate today = LocalDate.now();

        List<UserDailyMission> myToday = userDailyMissionRepository.findAllByUserIdAndMissionDate(userId, today);

        // 오늘 할당된 미션이 없으면 deterministic하게 sort_order 상위 3개 자동 할당
        if (myToday.isEmpty()) {
            List<DailyMission> activeMissions = dailyMissionRepository.findAllByUseYnOrderBySortOrderAsc("Y");
            int limit = Math.min(3, activeMissions.size());

            List<UserDailyMission> createdList = new ArrayList<>();
            for (int i = 0; i < limit; i++) {
                DailyMission dm = activeMissions.get(i);
                UserDailyMission udm = UserDailyMission.builder()
                        .userId(userId)
                        .missionId(dm.getMissionId())
                        .missionDate(today)
                        .build();
                createdList.add(userDailyMissionRepository.save(udm));
            }
            myToday = createdList;
        }

        List<DailyMissionResponse> responseList = new ArrayList<>();
        for (UserDailyMission udm : myToday) {
            DailyMission dm = dailyMissionRepository.findById(udm.getMissionId()).orElseThrow();
            String title = "KO".equalsIgnoreCase(lang) ? dm.getTitleKey() : dm.getTitleKey(); // 실 서비스에선 i18n 번역값 또는 리소스 바인딩
            String desc = "KO".equalsIgnoreCase(lang) ? dm.getDescriptionKey() : dm.getDescriptionKey();

            responseList.add(DailyMissionResponse.builder()
                    .missionId(dm.getMissionId())
                    .missionCode(dm.getMissionCode())
                    .title(title)
                    .description(desc)
                    .effectScore(dm.getEffectScore())
                    .completedYn("Y".equalsIgnoreCase(udm.getCompletedYn()))
                    .build());
        }

        return responseList;
    }

    /**
     * 미션 완료 처리 및 점수 차감 E2E (일일 한도 -9 상계, 단일 트랜잭션)
     */
    @Transactional
    public void completeMission(Long userId, Long missionId) {
        LocalDate today = LocalDate.now();

        UserDailyMission udm = userDailyMissionRepository.findByUserIdAndMissionIdAndMissionDate(userId, missionId, today)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if ("Y".equalsIgnoreCase(udm.getCompletedYn())) {
            throw new BusinessException(ErrorCode.ACTION_DUPLICATE); // 중복 완료 방지
        }

        DailyMission dm = dailyMissionRepository.findById(missionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 일일 미션 누적 차감량 abs(sum(effectScore)) 계산
        int sumScore = effectLogRepository.sumEffectScoreByDateAndType(userId, today, "DAILY_MISSION");
        int alreadyUsed = Math.abs(sumScore);
        int remaining = 9 - alreadyUsed;

        int missionEffect = dm.getEffectScore(); // 예: -2 or -3
        int effectScore = 0;
        String limitExceeded = "Y";

        if (remaining >= Math.abs(missionEffect)) {
            effectScore = missionEffect;
            limitExceeded = "N";

            // 내 오늘 자 마음컵 맑게 갱신
            DailyCup cup = dailyCupRepository.findByUserIdAndRecordDate(userId, today)
                    .orElseGet(() -> dailyCupRepository.save(DailyCup.builder()
                            .userId(userId)
                            .recordDate(today)
                            .pollutionScore(50)
                            .activeWorldLevel("SMALL_CUP")
                            .build()));
            cup.updatePollutionScore(cup.getPollutionScore() + effectScore);
            dailyCupRepository.save(cup);
        }

        // 미션 완료 설정
        udm.complete();
        userDailyMissionRepository.save(udm);

        // 효과 로그 기록 (sourceId로 userDailyMissionId 바인딩)
        effectLogRepository.save(MindScoreEffectLog.builder()
                .userId(userId)
                .sourceType("DAILY_MISSION")
                .sourceId(udm.getUserDailyMissionId())
                .effectScore(effectScore)
                .effectDate(today)
                .limitExceededYn(limitExceeded)
                .build());

        // E2E 단일 트랜잭션 하에 세계 성장 동기 업데이트
        worldService.recalculateWorldLevel(userId);

        // 업적 조건 평가 동기 트리거
        achievementService.evaluate(userId, TriggerType.DAILY_MISSION_COMPLETED);
    }
}
