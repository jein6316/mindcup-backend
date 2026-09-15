package com.mindcup.backend.global.init;

import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.domain.world.service.WorldService;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.WorldLevelRepository;
import com.mindcup.backend.domain.world.entity.EcosystemCreature;
import com.mindcup.backend.domain.world.repository.EcosystemCreatureRepository;
import com.mindcup.backend.domain.world.entity.WorldItem;
import com.mindcup.backend.domain.world.repository.WorldItemRepository;
import com.mindcup.backend.domain.mission.entity.DailyMission;
import com.mindcup.backend.domain.mission.repository.DailyMissionRepository;
import com.mindcup.backend.domain.achievement.entity.Achievement;
import com.mindcup.backend.domain.achievement.repository.AchievementRepository;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.entity.Fish;
import com.mindcup.backend.domain.cup.repository.FishRepository;
import com.mindcup.backend.domain.comfort.entity.ComfortMessageTemplate;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageTemplateRepository;
import com.mindcup.backend.domain.friend.entity.Friend;
import com.mindcup.backend.domain.friend.repository.FriendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WorldService worldService;
    private final DailyCupRepository dailyCupRepository;
    private final FriendRepository friendRepository;

    private final FishRepository fishRepository;
    private final ComfortMessageTemplateRepository comfortMessageTemplateRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final EcosystemCreatureRepository ecosystemCreatureRepository;
    private final WorldItemRepository worldItemRepository;
    private final DailyMissionRepository dailyMissionRepository;
    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        // 0. 마스터 데이터 시딩 (DB가 완전히 비어있을 때 최초 1회만 적재)
        seedMasterData();

        String test1Email = "test@test.com";
        String test2Email = "test2@test.com";

        User savedUser1 = null;
        User savedUser2 = null;

        // 1. test@test.com 생성
        if (!userRepository.existsByEmail(test1Email)) {
            User testUser = User.builder()
                    .email(test1Email)
                    .password(passwordEncoder.encode("123456"))
                    .nickname("test")
                    .provider("LOCAL")
                    .friendCode("TEST12")
                    .isStatusPublic(true)
                    .languageSetting("KO")
                    .unlockedWorldLevel("SMALL_CUP")
                    .build();

            savedUser1 = userRepository.save(testUser);
            log.info("로컬 테스트용 계정1이 자동 생성되었습니다. 이메일: {}, 패스워드: 123456", test1Email);

            worldService.recalculateWorldLevel(savedUser1.getUserId());
        } else {
            savedUser1 = userRepository.findByEmail(test1Email).orElse(null);
        }

        // 2. test2@test.com 생성 (최상의 맑음 레벨 - SEA 세팅)
        if (!userRepository.existsByEmail(test2Email)) {
            User testUser2 = User.builder()
                    .email(test2Email)
                    .password(passwordEncoder.encode("123456"))
                    .nickname("test2")
                    .provider("LOCAL")
                    .friendCode("TEST34")
                    .isStatusPublic(true)
                    .languageSetting("KO")
                    .unlockedWorldLevel("SEA")
                    .build();

            savedUser2 = userRepository.save(testUser2);
            log.info("로컬 테스트용 계정2(최상 SEA 레벨)가 자동 생성되었습니다. 이메일: {}, 패스워드: 123456", test2Email);

            // test2 의 7일간 컵 탁도를 0점(맑음도 100%)으로 강제 적재하여 평균 맑음도 100% 확보
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate date = today.minusDays(i);
                if (!dailyCupRepository.findByUserIdAndRecordDate(savedUser2.getUserId(), date).isPresent()) {
                    dailyCupRepository.save(DailyCup.builder()
                            .userId(savedUser2.getUserId())
                            .recordDate(date)
                            .pollutionScore(0) // 탁도 0%
                            .activeWorldLevel("SEA")
                            .build());
                }
            }

            // SEA 해금에 따른 모든 수집 보상 적재 연동 (중간 보상들 누적 지급)
            worldService.recalculateWorldLevel(savedUser2.getUserId());
        } else {
            savedUser2 = userRepository.findByEmail(test2Email).orElse(null);
        }

        // 3. test와 test2 친구 상호 관계 설정
        if (savedUser1 != null && savedUser2 != null) {
            Long user1Id = savedUser1.getUserId();
            Long user2Id = savedUser2.getUserId();

            if (!friendRepository.findRelation(user1Id, user2Id).isPresent()) {
                friendRepository.save(Friend.builder()
                        .userId(user1Id)
                        .targetUserId(user2Id)
                        .status("ACCEPTED")
                        .build());
                log.info("테스트 계정 test 와 test2 가 상호 친구 관계로 자동 연계되었습니다.");
            }
        }
    }

    private void seedMasterData() {
        // 1. 물고기 데이터 시드
        if (fishRepository.count() == 0) {
            fishRepository.saveAll(List.of(
                new Fish("은빛 송사리", "Silvery Minnow", "CLEAR", "물이 맑고 깨끗해져서 기분 좋게 넓은 곳을 활발히 유영하고 있어요.", "The water has become clean and clear, and the fish is swimming around happily in a wide space."),
                new Fish("은빛 송사리", "Silvery Minnow", "SLIGHTLY_CLOUDY", "약간 흐린 기운이 돌지만, 느긋하고 천천히 꼬리를 흔들며 머무르고 있어요.", "It is slightly cloudy, but the fish is staying and waving its tail slowly and peacefully."),
                new Fish("은빛 송사리", "Silvery Minnow", "CLOUDY", "조금 흐려진 날씨 탓에, 움직임을 최소화하고 물속 중앙에 잔잔히 머물러 있어요.", "Due to the slightly cloudy water, the fish is minimizing movement and resting quietly in the center."),
                new Fish("은빛 송사리", "Silvery Minnow", "VERY_CLOUDY", "물이 어두워져서 무서운 것은 아니에요. 수초 숲 포근한 침대 아래에서 조용히 잠을 청하며 내일을 기다리고 있어요.", "It is not scared just because the water got dark. It is sleeping quietly under the cozy water plants, waiting for tomorrow.")
            ));
            log.info("Fish 마스터 데이터 적재 완료.");
        }

        // 2. 위로 문구 템플릿 시드
        if (comfortMessageTemplateRepository.count() == 0) {
            comfortMessageTemplateRepository.saveAll(List.of(
                new ComfortMessageTemplate("오늘도 수고 많았어요. 마음이 금세 맑아지길 바랄게요.", "You did great today. Hope your mind clears up soon."),
                new ComfortMessageTemplate("흐린 날도 괜찮아요. 천천히 쉬어가도 괜찮습니다.", "It is okay to have cloudy days. Take your time to rest."),
                new ComfortMessageTemplate("언제나 당신의 하루를 응원합니다. 편안한 밤 되세요.", "Always rooting for you. Have a peaceful night."),
                new ComfortMessageTemplate("작은 돌봄이 모여 아름다운 바다가 될 거예요.", "Small cares will gather to make a beautiful sea.")
            ));
            log.info("ComfortMessageTemplate 마스터 데이터 적재 완료.");
        }

        // 3. 세계 레벨 마스터 데이터 시드
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.saveAll(List.of(
                new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"),
                new WorldLevel("LARGE_CUP", "world.largeCup", 30, 2, "Y"),
                new WorldLevel("AQUARIUM", "world.aquarium", 45, 3, "Y"),
                new WorldLevel("POND", "world.pond", 60, 4, "Y"),
                new WorldLevel("STREAM", "world.stream", 70, 5, "Y"),
                new WorldLevel("RIVER", "world.river", 80, 6, "Y"),
                new WorldLevel("LAKE", "world.lake", 88, 7, "Y"),
                new WorldLevel("SEA", "world.sea", 92, 8, "Y")
            ));
            log.info("WorldLevel 마스터 데이터 적재 완료.");
        }

        // 4. 생태계 생물 마스터 데이터 시드
        if (ecosystemCreatureRepository.count() == 0) {
            ecosystemCreatureRepository.saveAll(List.of(
                new EcosystemCreature("SMALL_CUP", "SILVER_MINNOW", "creature.silverMinnow", "FISH", "Y", 1, "Y"),
                new EcosystemCreature("LARGE_CUP", "BABY_SNAIL", "creature.babySnail", "SHELL", "Y", 1, "Y"),
                new EcosystemCreature("AQUARIUM", "GUPPY", "creature.guppy", "FISH", "Y", 1, "Y"),
                new EcosystemCreature("AQUARIUM", "GOLDFISH", "creature.goldfish", "FISH", "N", 2, "Y"),
                new EcosystemCreature("POND", "FROG", "creature.frog", "AMPHIBIAN", "Y", 1, "Y"),
                new EcosystemCreature("POND", "CARP", "creature.carp", "FISH", "N", 2, "Y"),
                new EcosystemCreature("STREAM", "STREAM_MINNOW", "creature.streamMinnow", "FISH", "Y", 1, "Y"),
                new EcosystemCreature("STREAM", "CRAWFISH", "creature.crawfish", "CRUSTACEAN", "N", 2, "Y"),
                new EcosystemCreature("RIVER", "RIVER_CARP", "creature.riverCarp", "FISH", "Y", 1, "Y"),
                new EcosystemCreature("RIVER", "DUCK", "creature.duck", "BIRD", "N", 2, "Y"),
                new EcosystemCreature("LAKE", "TURTLE", "creature.turtle", "REPTILE", "Y", 1, "Y"),
                new EcosystemCreature("LAKE", "SWAN", "creature.swan", "BIRD", "N", 2, "Y"),
                new EcosystemCreature("SEA", "WHALE", "creature.whale", "MAMMAL", "Y", 1, "Y"),
                new EcosystemCreature("SEA", "JELLYFISH", "creature.jellyfish", "INVERTEBRATE", "N", 2, "Y")
            ));
            log.info("EcosystemCreature 마스터 데이터 적재 완료.");
        }

        // 5. 꾸미기 조경 마스터 아이템 시드
        if (worldItemRepository.count() == 0) {
            worldItemRepository.saveAll(List.of(
                new WorldItem("SMALL_CUP", "BASIC_PEBBLE", "item.basicPebble", "STONE", "STONE", 1, "Y"),
                new WorldItem("LARGE_CUP", "BABY_SPROUT", "item.babySprout", "PLANT", "PLANT", 1, "Y"),
                new WorldItem("AQUARIUM", "WARM_LIGHT", "item.warmLight", "LIGHT", "LIGHT", 1, "Y"),
                new WorldItem("POND", "LOTUS_LEAF", "item.lotusLeaf", "DECORATION", "DECORATION", 1, "Y"),
                new WorldItem("STREAM", "STREAM_LEAF", "item.streamLeaf", "DECORATION", "DECORATION", 1, "Y"),
                new WorldItem("RIVER", "RIVER_REED", "item.riverReed", "PLANT", "PLANT", 1, "Y"),
                new WorldItem("LAKE", "LAKE_MOON", "item.lakeMoon", "BACKGROUND", "BACKGROUND", 1, "Y"),
                new WorldItem("SEA", "SEA_CORAL", "item.seaCoral", "STONE", "STONE", 1, "Y")
            ));
            log.info("WorldItem 마스터 데이터 적재 완료.");
        }

        // 6. 일일 미션 마스터 데이터 시드
        if (dailyMissionRepository.count() == 0) {
            dailyMissionRepository.saveAll(List.of(
                new DailyMission("dm_water", "SELF_CARE", "mission.water.title", "mission.water.desc", -2, 1, "Y"),
                new DailyMission("dm_breath", "REST", "mission.breath.title", "mission.breath.desc", -3, 2, "Y"),
                new DailyMission("dm_walk", "MOVEMENT", "mission.walk.title", "mission.walk.desc", -3, 3, "Y")
            ));
            log.info("DailyMission 마스터 데이터 적재 완료.");
        }

        // 7. 업적 마스터 데이터 시드
        if (achievementRepository.count() == 0) {
            achievementRepository.saveAll(List.of(
                new Achievement("ach_first_clear", "ach.first_clear.title", "ach.first_clear.desc", "CLEAR_ACTION_COUNT", 1, null, null, 1, "Y"),
                new Achievement("ach_7_clear", "ach.7_clear.title", "ach.7_clear.desc", "CLEAR_ACTION_COUNT", 7, null, null, 2, "Y"),
                new Achievement("ach_cloudy_care", "ach.cloudy_care.title", "ach.cloudy_care.desc", "RECORD_IN_VERY_CLOUDY", 1, null, null, 3, "Y"),
                new Achievement("ach_big_recovery", "ach.big_rec.title", "ach.big_rec.desc", "POLLUTION_DECREASE_IN_DAY", 10, null, null, 4, "Y"),
                new Achievement("ach_first_send", "ach.first_send.title", "ach.first_send.desc", "COMFORT_SENT_COUNT", 1, null, null, 5, "Y"),
                new Achievement("ach_10_send", "ach.10_send.title", "ach.10_send.desc", "COMFORT_SENT_COUNT", 10, null, null, 6, "Y"),
                new Achievement("ach_stream", "ach.stream.title", "ach.stream.desc", "WORLD_UNLOCKED", 5, null, null, 7, "Y"),
                new Achievement("ach_river", "ach.river.title", "ach.river.desc", "WORLD_UNLOCKED", 6, null, null, 8, "Y"),
                new Achievement("ach_lake", "ach.lake.title", "ach.lake.desc", "WORLD_UNLOCKED", 7, null, null, 9, "Y"),
                new Achievement("ach_sea", "ach.sea.title", "ach.sea.desc", "WORLD_UNLOCKED", 8, null, null, 10, "Y")
            ));
            log.info("Achievement 마스터 데이터 적재 완료.");
        }
    }
}
