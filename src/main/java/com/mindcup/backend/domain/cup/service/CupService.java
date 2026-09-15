package com.mindcup.backend.domain.cup.service;

import com.mindcup.backend.domain.cup.dto.CustomActionRequest;
import com.mindcup.backend.domain.cup.dto.CustomActionResponse;
import com.mindcup.backend.domain.cup.dto.DailyCupResponse;
import com.mindcup.backend.domain.cup.dto.RecordRequest;
import com.mindcup.backend.domain.cup.dto.RecordResponse;
import com.mindcup.backend.domain.cup.dto.WeeklyStatsResponse;
import com.mindcup.backend.domain.cup.entity.CustomAction;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.Fish;
import com.mindcup.backend.domain.cup.entity.MindRecord;
import com.mindcup.backend.domain.cup.repository.CustomActionRepository;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.FishRepository;
import com.mindcup.backend.domain.cup.repository.MindRecordRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.domain.world.service.WorldService;
import com.mindcup.backend.domain.achievement.entity.TriggerType;
import com.mindcup.backend.domain.achievement.service.AchievementService;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CupService {

    private final DailyCupRepository dailyCupRepository;
    private final MindRecordRepository mindRecordRepository;
    private final CustomActionRepository customActionRepository;
    private final FishRepository fishRepository;
    private final UserRepository userRepository;
    private final WorldService worldService;

    @org.springframework.beans.factory.annotation.Autowired
    @Lazy
    private AchievementService achievementService;

    // 세계 성장 해금 임계치 매핑
    private static final Map<String, Integer> WORLD_THRESHOLDS = new HashMap<>();
    private static final List<String> WORLD_LEVEL_ORDER = List.of(
            "SMALL_CUP", "LARGE_CUP", "AQUARIUM", "POND", "STREAM", "RIVER", "LAKE", "SEA"
    );

    static {
        WORLD_THRESHOLDS.put("SMALL_CUP", 0);
        WORLD_THRESHOLDS.put("LARGE_CUP", 30);
        WORLD_THRESHOLDS.put("AQUARIUM", 45);
        WORLD_THRESHOLDS.put("POND", 60);
        WORLD_THRESHOLDS.put("STREAM", 70);
        WORLD_THRESHOLDS.put("RIVER", 80);
        WORLD_THRESHOLDS.put("LAKE", 88);
        WORLD_THRESHOLDS.put("SEA", 92);
    }

    @Transactional
    public DailyCupResponse getOrCreateTodayCup(Long userId) {
        LocalDate today = LocalDate.now();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        DailyCup dailyCup = dailyCupRepository.findByUserIdAndRecordDate(userId, today)
                .orElseGet(() -> {
                    // 전일 최종 탁도 상계 조회
                    Optional<DailyCup> prevCup = dailyCupRepository
                            .findFirstByUserIdAndRecordDateBeforeOrderByRecordDateDesc(userId, today);
                    int initialPollution = prevCup.map(DailyCup::getPollutionScore).orElse(50);

                    DailyCup newCup = DailyCup.builder()
                            .userId(userId)
                            .recordDate(today)
                            .pollutionScore(initialPollution)
                            .activeWorldLevel(user.getUnlockedWorldLevel())
                            .build();

                    return dailyCupRepository.save(newCup);
                });

        Fish fish = fishRepository.findByStatus(dailyCup.getCupStatus())
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));

        boolean isKorean = "KO".equalsIgnoreCase(user.getLanguageSetting());
        return DailyCupResponse.of(dailyCup, fish, isKorean);
    }

    @Transactional
    public RecordResponse submitCheckSurvey(Long userId, Integer pollutionScore) {
        LocalDate today = LocalDate.now();
        DailyCup dailyCup = dailyCupRepository.findByUserIdAndRecordDate(userId, today)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                    return dailyCupRepository.save(DailyCup.builder()
                            .userId(userId)
                            .recordDate(today)
                            .pollutionScore(50)
                            .activeWorldLevel(user.getUnlockedWorldLevel())
                            .build());
                });

        // 이미 오늘 진단 설문 기록이 있는지 체크
        List<MindRecord> records = mindRecordRepository.findAllByDailyCup(dailyCup);
        boolean alreadySurveyed = records.stream()
                .anyMatch(r -> "오늘의 마음 진단".equals(r.getActionName()));

        if (alreadySurveyed) {
            throw new BusinessException(ErrorCode.CUP_ALREADY_CHECKED);
        }

        // 오늘 컵 점수 업데이트
        dailyCup.updatePollutionScore(pollutionScore);

        // 특별 기록 생성
        MindRecord surveyRecord = MindRecord.builder()
                .dailyCup(dailyCup)
                .recordType("TURBID") // 기본값 TURBID 설정
                .actionName("오늘의 마음 진단")
                .intensity(3)
                .memo("오늘 하루를 여는 마음의 날씨를 확인했습니다.")
                .build();

        mindRecordRepository.save(surveyRecord);

        // 누적 최고 해금 조건 체크
        updateWorldLockStatus(userId);

        // 2단계 세계 성장 실시간 계산 반영
        worldService.recalculateWorldLevel(userId);

        // 업적 트리거 동기 평가
        achievementService.evaluate(userId, TriggerType.CLOUDY_ACTION_CREATED);
        achievementService.evaluate(userId, TriggerType.POLLUTION_SCORE_CHANGED);

        return RecordResponse.of(surveyRecord, dailyCup);
    }

    @Transactional
    public RecordResponse addRecord(Long userId, RecordRequest request) {
        LocalDate today = LocalDate.now();
        DailyCup dailyCup = dailyCupRepository.findByUserIdAndRecordDate(userId, today)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
                    // 전일 점수 기반
                    Optional<DailyCup> prevCup = dailyCupRepository
                            .findFirstByUserIdAndRecordDateBeforeOrderByRecordDateDesc(userId, today);
                    int initialPollution = prevCup.map(DailyCup::getPollutionScore).orElse(50);
                    return dailyCupRepository.save(DailyCup.builder()
                            .userId(userId)
                            .recordDate(today)
                            .pollutionScore(initialPollution)
                            .activeWorldLevel(user.getUnlockedWorldLevel())
                            .build());
                });

        int scoreChange;
        if ("CLEAR".equals(request.getRecordType())) {
            scoreChange = -(request.getIntensity() * 4);
        } else {
            scoreChange = (request.getIntensity() * 3);
        }

        dailyCup.updatePollutionScore(dailyCup.getPollutionScore() + scoreChange);

        MindRecord record = MindRecord.builder()
                .dailyCup(dailyCup)
                .recordType(request.getRecordType())
                .actionName(request.getActionName())
                .intensity(request.getIntensity())
                .memo(request.getMemo())
                .build();

        mindRecordRepository.save(record);

        // 세계 해금 체크 및 업데이트
        updateWorldLockStatus(userId);

        // 2단계 세계 성장 실시간 계산 반영
        worldService.recalculateWorldLevel(userId);

        // 업적 트리거 동기 평가
        if ("CLEAR".equals(request.getRecordType())) {
            achievementService.evaluate(userId, TriggerType.CLEAR_ACTION_CREATED);
        } else {
            achievementService.evaluate(userId, TriggerType.CLOUDY_ACTION_CREATED);
        }
        achievementService.evaluate(userId, TriggerType.POLLUTION_SCORE_CHANGED);

        if (dailyCup.getPollutionScore() >= 71) {
            achievementService.evaluate(userId, TriggerType.RECORD_IN_VERY_CLOUDY);
        }

        return RecordResponse.of(record, dailyCup);
    }

    public WeeklyStatsResponse getWeeklyStats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<DailyCup> existCups = dailyCupRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, today);

        // 7일간 빈 데이터 채워 넣기
        Map<LocalDate, Integer> scoreMap = new HashMap<>();
        for (DailyCup cup : existCups) {
            scoreMap.put(cup.getRecordDate(), cup.getClarityScore());
        }

        List<WeeklyStatsResponse.DailyStat> history = new ArrayList<>();
        double totalClarity = 0.0;
        int defaultClarity = 50;

        for (int i = 0; i < 7; i++) {
            LocalDate date = start.plusDays(i);
            int clarity;
            if (scoreMap.containsKey(date)) {
                clarity = scoreMap.get(date);
            } else {
                // 연속성을 위해 이전 날짜 데이터 탐색
                clarity = defaultClarity;
                for (int j = i - 1; j >= 0; j--) {
                    LocalDate checkDate = start.plusDays(j);
                    if (scoreMap.containsKey(checkDate)) {
                        clarity = scoreMap.get(checkDate);
                        break;
                    }
                }
            }
            history.add(new WeeklyStatsResponse.DailyStat(date, clarity));
            totalClarity += clarity;
            defaultClarity = clarity; // 다음 공백을 채울 때 승계
        }

        double averageClarity = totalClarity / 7.0;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return new WeeklyStatsResponse(averageClarity, user.getUnlockedWorldLevel(), history);
    }

    @Transactional
    public CustomActionResponse createCustomAction(Long userId, CustomActionRequest request) {
        Optional<CustomAction> exist = customActionRepository
                .findByUserIdAndActionNameAndActionType(userId, request.getActionName(), request.getActionType());

        if (exist.isPresent()) {
            throw new BusinessException(ErrorCode.ACTION_DUPLICATE);
        }

        CustomAction action = CustomAction.builder()
                .userId(userId)
                .actionName(request.getActionName())
                .actionType(request.getActionType())
                .build();

        customActionRepository.save(action);
        return CustomActionResponse.from(action);
    }

    public List<CustomActionResponse> getCustomActions(Long userId) {
        return customActionRepository.findAllByUserId(userId).stream()
                .map(CustomActionResponse::from)
                .toList();
    }

    @Transactional
    public void deleteCustomAction(Long userId, Long actionId) {
        CustomAction action = customActionRepository.findById(actionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTION_NOT_FOUND));

        if (!action.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        customActionRepository.delete(action);
    }

    public Fish getFishByStatus(String status) {
        return fishRepository.findByStatus(status)
                .orElseThrow(() -> new BusinessException(ErrorCode.FISH_NOT_FOUND));
    }

    private void updateWorldLockStatus(Long userId) {
        WeeklyStatsResponse stats = getWeeklyStats(userId);
        double avgClarity = stats.getAverageClarity();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        String currentUnlocked = user.getUnlockedWorldLevel();
        String targetUnlocked = "SMALL_CUP";

        for (String level : WORLD_LEVEL_ORDER) {
            int threshold = WORLD_THRESHOLDS.get(level);
            if (avgClarity >= threshold) {
                targetUnlocked = level;
            }
        }

        // 비가역성: targetUnlocked의 순서 인덱스가 currentUnlocked의 인덱스보다 클 때만 갱신
        int currentIndex = WORLD_LEVEL_ORDER.indexOf(currentUnlocked);
        int targetIndex = WORLD_LEVEL_ORDER.indexOf(targetUnlocked);

        if (targetIndex > currentIndex) {
            user.updateUnlockedWorldLevel(targetUnlocked);
            // 오늘 마음컵의 활성 세계 단계도 동반 갱신
            DailyCup todayCup = dailyCupRepository.findByUserIdAndRecordDate(userId, LocalDate.now()).orElse(null);
            if (todayCup != null) {
                todayCup.updateActiveWorldLevel(targetUnlocked);
            }
            log.info("User {} unlocked new world level: {}", userId, targetUnlocked);
            // 업적 트리거 동기 평가
            achievementService.evaluate(userId, TriggerType.WORLD_UNLOCKED);
        }
    }
}
