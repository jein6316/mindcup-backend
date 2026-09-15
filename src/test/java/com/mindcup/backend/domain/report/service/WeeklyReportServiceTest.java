package com.mindcup.backend.domain.report.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindRecord;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindRecordRepository;
import com.mindcup.backend.domain.report.dto.WeeklyReportResponse;
import com.mindcup.backend.domain.report.entity.WeeklyReport;
import com.mindcup.backend.domain.report.repository.WeeklyReportRepository;
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
public class WeeklyReportServiceTest {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private MindRecordRepository mindRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
        }

        testUser = userRepository.save(User.builder().email("report@mindcup.com").password("pass").nickname("ReportTester").provider("LOCAL").friendCode("REP123").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());
    }

    @Test
    void testEmptyDataReturnsEncouragementKey() {
        Long userId = testUser.getUserId();
        WeeklyReportResponse response = weeklyReportService.getLatestWeeklyReport(userId);
        
        // 기록이 아예 없는 경우 격려 메시지 반환 검증
        assertThat(response.getReportMessage()).contains("오늘의 한 방울부터 함께");
        assertThat(response.getAverageClarityScore()).isEqualTo(50);
    }

    @Test
    void testStatisticalCalculationAndDuplicateUpdatesReport() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // 7일간 중 3일만 기록 등록 (기록 없는 날은 분모 제외 통계 검증)
        DailyCup cup1 = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(10).activeWorldLevel("SMALL_CUP").build()); // clarity = 90
        DailyCup cup2 = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today.minusDays(2)).pollutionScore(20).activeWorldLevel("SMALL_CUP").build()); // clarity = 80
        DailyCup cup3 = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today.minusDays(5)).pollutionScore(30).activeWorldLevel("SMALL_CUP").build()); // clarity = 70

        // 맑음 행동 기록 추가
        mindRecordRepository.save(MindRecord.builder().dailyCup(cup1).recordType("CLEAR").actionName("따뜻한 차 마시기").intensity(3).memo("좋았다").build());
        mindRecordRepository.save(MindRecord.builder().dailyCup(cup2).recordType("CLEAR").actionName("따뜻한 차 마시기").intensity(4).memo("차분했다").build());
        mindRecordRepository.save(MindRecord.builder().dailyCup(cup3).recordType("TURBID").actionName("업무 과로").intensity(2).memo("힘들었다").build());

        // 1. 리포트 생성
        WeeklyReport report = weeklyReportService.generateWeeklyReport(userId);
        
        // 평균 맑음도 80점 검증 ((90+80+70)/3 = 80)
        assertThat(report.getAverageClarityScore()).isEqualTo(80);
        assertThat(report.getMostCommonClearAction()).isEqualTo("따뜻한 차 마시기");
        assertThat(report.getMostCommonCloudyAction()).isEqualTo("업무 과로");

        // 2. 중복 생성 호출 시 기존 레코드 UPDATE 검증
        dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today.minusDays(1)).pollutionScore(0).activeWorldLevel("SMALL_CUP").build()); // clarity = 100
        WeeklyReport updatedReport = weeklyReportService.generateWeeklyReport(userId);
        
        // 데이터가 4건으로 늘어 평균 85점으로 갱신되었는지 검증 ((90+80+70+100)/4 = 85)
        assertThat(updatedReport.getAverageClarityScore()).isEqualTo(85);
        assertThat(weeklyReportRepository.count()).isEqualTo(1); // unique 쌍이므로 row 수는 1개 유지 확인
    }
}
