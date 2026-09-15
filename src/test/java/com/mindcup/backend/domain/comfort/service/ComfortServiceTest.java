package com.mindcup.backend.domain.comfort.service;

import com.mindcup.backend.domain.comfort.entity.ComfortMessage;
import com.mindcup.backend.domain.comfort.entity.ComfortMessageTemplate;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageRepository;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageTemplateRepository;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindScoreEffectLog;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindScoreEffectLogRepository;
import com.mindcup.backend.domain.friend.dto.FriendMindStatusResponse;
import com.mindcup.backend.domain.friend.entity.Friend;
import com.mindcup.backend.domain.friend.entity.UserFriendSetting;
import com.mindcup.backend.domain.friend.repository.FriendRepository;
import com.mindcup.backend.domain.friend.repository.UserFriendSettingRepository;
import com.mindcup.backend.domain.friend.service.FriendService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ComfortServiceTest {

    @Autowired
    private ComfortService comfortService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserFriendSettingRepository userFriendSettingRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private ComfortMessageRepository comfortMessageRepository;

    @Autowired
    private ComfortMessageTemplateRepository templateRepository;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    @Autowired
    private MindScoreEffectLogRepository effectLogRepository;

    private User userA;
    private User userB;
    private User userC;
    private ComfortMessageTemplate template;

    @BeforeEach
    void setUp() {
        // H2 테스트 지원을 위한 마스터 레벨 강제 적재
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
            worldLevelRepository.save(new WorldLevel("LARGE_CUP", "world.largeCup", 30, 2, "Y"));
        }

        // 테스트 유저들 생성
        userA = userRepository.save(User.builder().email("userA@mindcup.com").password("pass").nickname("UserA").provider("LOCAL").friendCode("CODEA").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());
        userB = userRepository.save(User.builder().email("userB@mindcup.com").password("pass").nickname("UserB").provider("LOCAL").friendCode("CODEB").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());
        userC = userRepository.save(User.builder().email("userC@mindcup.com").password("pass").nickname("UserC").provider("LOCAL").friendCode("CODEC").isStatusPublic(true).languageSetting("KO").unlockedWorldLevel("SMALL_CUP").build());

        // 위로 템플릿 적재
        template = templateRepository.save(new ComfortMessageTemplate("힘내요!", "Cheer up!"));
    }

    private void changeVisibility(Long userId, String level) {
        UserFriendSetting s = userFriendSettingRepository.findByUserId(userId).orElse(null);
        if (s == null) {
            userFriendSettingRepository.save(UserFriendSetting.builder().userId(userId).visibilityLevel(level).build());
        } else {
            s.updateVisibilityLevel(level);
            userFriendSettingRepository.save(s);
        }
    }

    @Test
    void testPrivacyMaskingByVisibilityLevel() {
        Long idA = userA.getUserId();
        Long idB = userB.getUserId();

        // A와 B 친구 맺기
        friendRepository.save(Friend.builder().userId(idA).targetUserId(idB).status("ACCEPTED").build());

        // 오늘 B가 기록 작성 (점수 10점 -> CLEAR 상태)
        dailyCupRepository.save(DailyCup.builder().userId(idB).recordDate(LocalDate.now()).pollutionScore(10).activeWorldLevel("SMALL_CUP").build());

        // 1. B의 설정이 PRIVATE 인 경우 -> 전부 마스킹(null)
        changeVisibility(idB, "PRIVATE");
        FriendMindStatusResponse response = friendService.getSingleFriendMindStatus(idA, idB);
        assertThat(response.getRecordedToday()).isNull();
        assertThat(response.getMindCupLevel()).isNull();

        // 2. B의 설정이 RECORD_STATUS_ONLY 인 경우 -> 오늘기록여부만 공개
        changeVisibility(idB, "RECORD_STATUS_ONLY");
        response = friendService.getSingleFriendMindStatus(idA, idB);
        assertThat(response.getRecordedToday()).isTrue();
        assertThat(response.getMindCupLevel()).isNull();

        // 3. B의 설정이 CUP_LEVEL_ONLY 인 경우 -> 오늘기록여부 및 컵상태(CLEAR) 공개
        changeVisibility(idB, "CUP_LEVEL_ONLY");
        response = friendService.getSingleFriendMindStatus(idA, idB);
        assertThat(response.getRecordedToday()).isTrue();
        assertThat(response.getMindCupLevel()).isEqualTo("CLEAR");
    }

    @Test
    void testNonFriendAccessDenied() {
        Long idA = userA.getUserId();
        Long idC = userC.getUserId();

        // A와 C는 친구가 아님 -> 조회 시 403 Forbidden 발생 검증
        assertThatThrownBy(() -> friendService.getSingleFriendMindStatus(idA, idC))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testDuplicateComfortMessagePrevented() {
        Long idA = userA.getUserId();
        Long idB = userB.getUserId();

        // 친구 맺기
        friendRepository.save(Friend.builder().userId(idA).targetUserId(idB).status("ACCEPTED").build());

        // 1회 전송 성공
        comfortService.sendComfortMessage(idA, idB, template.getTemplateId());

        // 같은 친구에게 당일 2회 전송 시도 -> 중복 차단 검증
        assertThatThrownBy(() -> comfortService.sendComfortMessage(idA, idB, template.getTemplateId()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void testSendLimitUpperBoundary() {
        Long idA = userA.getUserId();
        Long idB = userB.getUserId();
        Long idC = userC.getUserId();

        // A-B, A-C 각각 친구 맺기
        friendRepository.save(Friend.builder().userId(idA).targetUserId(idB).status("ACCEPTED").build());
        friendRepository.save(Friend.builder().userId(idA).targetUserId(idC).status("ACCEPTED").build());

        // 오늘 A의 컵 상태 기정
        DailyCup aCup = dailyCupRepository.save(DailyCup.builder().userId(idA).recordDate(LocalDate.now()).pollutionScore(50).activeWorldLevel("SMALL_CUP").build());

        // 1회 전송 (-2 반영)
        comfortService.sendComfortMessage(idA, idB, template.getTemplateId());
        assertThat(aCup.getPollutionScore()).isEqualTo(48);

        // 여러 명에게 보내서 한도 -10을 채웠다고 시뮬레이션하기 위해 임의로 4회 전송 추가 적재
        // A -> B로 가짜 적재해둠으로써 A -> C 전송 시 중복 발송 차단(ACTION_DUPLICATE)과 충돌하지 않게 함
        for (int i = 0; i < 4; i++) {
            comfortMessageRepository.save(ComfortMessage.builder().senderUserId(idA).receiverUserId(idB).templateId(template.getTemplateId()).senderEffectScore(-2).receiverEffectScore(3).build());
            // 로그 강제 기입
            effectLogRepository.save(MindScoreEffectLog.builder()
                    .userId(idA)
                    .sourceType("COMFORT_SENT")
                    .sourceId(999L)
                    .effectScore(-2)
                    .effectDate(LocalDate.now())
                    .limitExceededYn("N")
                    .build());
        }

        // 이제 전송 시 누계 한도(-10)에 달했으므로 0점 적용 성공 확인
        comfortService.sendComfortMessage(idA, idC, template.getTemplateId());
        assertThat(aCup.getPollutionScore()).isEqualTo(48); // 추가 차감 없이 48 유지
    }

    @Test
    void testPourLimitExceededAndNStatePreserved() {
        Long idA = userA.getUserId();
        Long idB = userB.getUserId();

        // A가 보낸 물방울 6개 도착 시뮬레이션
        dailyCupRepository.save(DailyCup.builder().userId(idB).recordDate(LocalDate.now()).pollutionScore(50).activeWorldLevel("SMALL_CUP").build());

        // 붓기 한도 -15에 달하도록 이미 5회 소모한 로그 적재
        for (int i = 0; i < 5; i++) {
            effectLogRepository.save(MindScoreEffectLog.builder()
                    .userId(idB)
                    .sourceType("COMFORT_POURED")
                    .sourceId((long) i)
                    .effectScore(-3)
                    .effectDate(LocalDate.now())
                    .limitExceededYn("N")
                    .build());
        }

        // 6번째 부을 물방울 메시지
        ComfortMessage unpoured = comfortMessageRepository.save(ComfortMessage.builder().senderUserId(idA).receiverUserId(idB).templateId(template.getTemplateId()).senderEffectScore(-2).receiverEffectScore(3).build());

        // 붓기 시도 시 409 Conflict(BusinessException) 예외 터짐 검증
        assertThatThrownBy(() -> comfortService.pourComfortMessage(idB, unpoured.getComfortId()))
                .isInstanceOf(BusinessException.class);

        // 해당 메시지가 pouredYn = 'N'으로 안전 보존되었는지 검증
        ComfortMessage savedMsg = comfortMessageRepository.findById(unpoured.getComfortId()).orElseThrow();
        assertThat(savedMsg.getPouredYn()).isEqualTo("N");
    }
}
