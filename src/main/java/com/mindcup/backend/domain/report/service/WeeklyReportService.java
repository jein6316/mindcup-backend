package com.mindcup.backend.domain.report.service;

import com.mindcup.backend.domain.comfort.repository.ComfortMessageRepository;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindRecord;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindRecordRepository;
import com.mindcup.backend.domain.mission.repository.UserDailyMissionRepository;
import com.mindcup.backend.domain.report.dto.WeeklyReportResponse;
import com.mindcup.backend.domain.report.entity.WeeklyReport;
import com.mindcup.backend.domain.report.repository.WeeklyReportRepository;
import com.mindcup.backend.domain.world.entity.UserWorld;
import com.mindcup.backend.domain.world.repository.UserWorldRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final DailyCupRepository dailyCupRepository;
    private final MindRecordRepository mindRecordRepository;
    private final ComfortMessageRepository comfortMessageRepository;
    private final UserDailyMissionRepository userDailyMissionRepository;
    private final UserWorldRepository userWorldRepository;
    private final UserRepository userRepository;

    /**
     * 최신 주간 리포트 단건 조회 (없을 시 온디맨드 즉시 생성)
     */
    @Transactional
    public WeeklyReportResponse getLatestWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        // 당일 포함 최근 7일(start ~ today)의 리포트가 존재하는지 검사
        Optional<WeeklyReport> reportOpt = weeklyReportRepository
                .findByUserIdAndStartDateAndEndDate(userId, start, today);

        WeeklyReport report;
        if (reportOpt.isPresent()) {
            report = reportOpt.get();
        } else {
            // 존재하지 않으면 즉시 생성
            report = generateWeeklyReport(userId);
        }

        return convertToResponse(userId, report);
    }

    /**
     * 주간 리포트 수동 생성 및 중복 재생성 (존재 시 UPDATE, 없을 시 INSERT)
     */
    @Transactional
    public WeeklyReport generateWeeklyReport(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<DailyCup> cups = dailyCupRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, today);

        UserWorld uw = userWorldRepository.findByUserId(userId).orElse(null);
        String worldLevel = uw != null ? uw.getUnlockedWorldLevel() : "SMALL_CUP";

        WeeklyReport report;

        // 1. 데이터가 아예 없는 휴면/신규 사용자의 경우 -> 격려 안내 문구 세팅 및 빈 값 저장
        if (cups.isEmpty()) {
            report = weeklyReportRepository.findByUserIdAndStartDateAndEndDate(userId, start, today)
                    .orElseGet(() -> WeeklyReport.builder()
                            .userId(userId)
                            .startDate(start)
                            .endDate(today)
                            .averageClarityScore(50)
                            .averagePollutionScore(50)
                            .unlockedWorldLevel(worldLevel)
                            .reportMessageKey("report.empty")
                            .build());

            if (report.getWeeklyReportId() != null) {
                report.updateReport(50, 50, null, null, null, 0, 0, 0, worldLevel, "report.empty");
            }
            return weeklyReportRepository.save(report);
        }

        // 2. 기록 일자 기준 평균 계산 (기록 없는 날 제외)
        int size = cups.size();
        int sumPollution = cups.stream().mapToInt(DailyCup::getPollutionScore).sum();
        int sumClarity = cups.stream().mapToInt(DailyCup::getClarityScore).sum();
        int avgPollution = sumPollution / size;
        int avgClarity = sumClarity / size;

        // 행동 로그 로드
        List<MindRecord> records = mindRecordRepository.findAllRecordsInPeriod(userId, start, today);

        // 최빈 흐림 요인 (진단 설문 제외)
        String mostCloudyAction = records.stream()
                .filter(r -> "TURBID".equals(r.getRecordType()) && !"오늘의 마음 진단".equals(r.getActionName()))
                .collect(Collectors.groupingBy(MindRecord::getActionName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 최빈 맑음 행동
        String mostClearAction = records.stream()
                .filter(r -> "CLEAR".equals(r.getRecordType()))
                .collect(Collectors.groupingBy(MindRecord::getActionName, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 가장 효과가 좋았던 맑음 행동 (평균 abs(intensity * 4) 가 가장 큰 것)
        String mostEffectiveClearAction = records.stream()
                .filter(r -> "CLEAR".equals(r.getRecordType()))
                .collect(Collectors.groupingBy(MindRecord::getActionName,
                        Collectors.averagingDouble(r -> r.getIntensity() * 4)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 친구 상호작용 카운팅
        int sentDrops = (int) comfortMessageRepository.countBySenderUserIdAndSentDateBetween(userId, start, today);
        int pouredDrops = (int) comfortMessageRepository.countByReceiverUserIdAndPouredYnAndSentDateBetween(userId, "Y", start, today);

        // 완료 미션 카운팅
        int completedMissions = (int) userDailyMissionRepository.countByUserIdAndCompletedYnAndMissionDateBetween(userId, "Y", start, today);

        // 기존 존재 여부 확인 후 UPDATE / INSERT
        Optional<WeeklyReport> existReport = weeklyReportRepository
                .findByUserIdAndStartDateAndEndDate(userId, start, today);

        if (existReport.isPresent()) {
            report = existReport.get();
            report.updateReport(avgClarity, avgPollution, mostCloudyAction, mostClearAction, mostEffectiveClearAction, sentDrops, pouredDrops, completedMissions, worldLevel, "report.flow.normal");
        } else {
            report = WeeklyReport.builder()
                    .userId(userId)
                    .startDate(start)
                    .endDate(today)
                    .averageClarityScore(avgClarity)
                    .averagePollutionScore(avgPollution)
                    .mostCommonCloudyAction(mostCloudyAction)
                    .mostCommonClearAction(mostClearAction)
                    .mostEffectiveClearAction(mostEffectiveClearAction)
                    .sentDropCount(sentDrops)
                    .pouredDropCount(pouredDrops)
                    .completedMissionCount(completedMissions)
                    .unlockedWorldLevel(worldLevel)
                    .reportMessageKey("report.flow.normal")
                    .build();
        }

        return weeklyReportRepository.save(report);
    }

    /**
     * DTO 다국어 문구 바인딩 및 변환
     */
    private WeeklyReportResponse convertToResponse(Long userId, WeeklyReport report) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        boolean isKorean = "KO".equalsIgnoreCase(user.getLanguageSetting());

        String message;
        if ("report.empty".equals(report.getReportMessageKey())) {
            message = isKorean 
                ? "아직 돌아볼 기록이 많지 않아요. 오늘의 한 방울부터 함께 시작해볼까요?"
                : "There is not much to look back on yet. Start with one small drop today.";
        } else {
            message = isKorean
                ? "이번 주에는 이런 마음의 흐름이 있었어요. 마음은 언제든 다시 따뜻하게 돌볼 수 있어요."
                : "This was your flow this week. Your mind can always be cared for again.";
        }

        return WeeklyReportResponse.builder()
                .weeklyReportId(report.getWeeklyReportId())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .averageClarityScore(report.getAverageClarityScore())
                .averagePollutionScore(report.getAveragePollutionScore())
                .mostCommonCloudyAction(report.getMostCommonCloudyAction())
                .mostCommonClearAction(report.getMostCommonClearAction())
                .mostEffectiveClearAction(report.getMostEffectiveClearAction())
                .sentDropCount(report.getSentDropCount())
                .pouredDropCount(report.getPouredDropCount())
                .completedMissionCount(report.getCompletedMissionCount())
                .unlockedWorldLevel(report.getUnlockedWorldLevel())
                .reportMessage(message)
                .build();
    }
}
