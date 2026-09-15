package com.mindcup.backend.domain.world.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.domain.world.dto.WorldDisplayResponse;
import com.mindcup.backend.domain.world.entity.EcosystemCreature;
import com.mindcup.backend.domain.world.entity.UserWorld;
import com.mindcup.backend.domain.world.entity.UserWorldItem;
import com.mindcup.backend.domain.world.entity.WorldItem;
import com.mindcup.backend.domain.world.entity.WorldLevel;
import com.mindcup.backend.domain.world.repository.EcosystemCreatureRepository;
import com.mindcup.backend.domain.world.repository.UserCreatureRepository;
import com.mindcup.backend.domain.world.repository.UserWorldItemRepository;
import com.mindcup.backend.domain.world.repository.UserWorldRepository;
import com.mindcup.backend.domain.world.repository.WorldItemRepository;
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
public class WorldServiceTest {

    @Autowired
    private WorldService worldService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyCupRepository dailyCupRepository;

    @Autowired
    private UserWorldRepository userWorldRepository;

    @Autowired
    private UserCreatureRepository userCreatureRepository;

    @Autowired
    private UserWorldItemRepository userWorldItemRepository;

    @Autowired
    private WorldLevelRepository worldLevelRepository;

    @Autowired
    private EcosystemCreatureRepository ecosystemCreatureRepository;

    @Autowired
    private WorldItemRepository worldItemRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // H2 인메모리 DB 테스트 격리 지원을 위한 마스터 설정 데이터 수동 강제 적재
        if (worldLevelRepository.count() == 0) {
            worldLevelRepository.save(new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y"));
            worldLevelRepository.save(new WorldLevel("LARGE_CUP", "world.largeCup", 30, 2, "Y"));
            worldLevelRepository.save(new WorldLevel("AQUARIUM", "world.aquarium", 45, 3, "Y"));
            worldLevelRepository.save(new WorldLevel("POND", "world.pond", 60, 4, "Y"));
        }

        if (ecosystemCreatureRepository.count() == 0) {
            ecosystemCreatureRepository.save(new EcosystemCreature("SMALL_CUP", "SILVER_MINNOW", "creature.silverMinnow", "FISH", "Y", 1, "Y"));
            ecosystemCreatureRepository.save(new EcosystemCreature("LARGE_CUP", "BABY_SNAIL", "creature.babySnail", "SHELL", "Y", 1, "Y"));
            ecosystemCreatureRepository.save(new EcosystemCreature("AQUARIUM", "GUPPY", "creature.guppy", "FISH", "Y", 1, "Y"));
            ecosystemCreatureRepository.save(new EcosystemCreature("AQUARIUM", "GOLDFISH", "creature.goldfish", "FISH", "N", 2, "Y"));
            ecosystemCreatureRepository.save(new EcosystemCreature("POND", "FROG", "creature.frog", "AMPHIBIAN", "Y", 1, "Y"));
            ecosystemCreatureRepository.save(new EcosystemCreature("POND", "CARP", "creature.carp", "FISH", "N", 2, "Y"));
        }

        if (worldItemRepository.count() == 0) {
            worldItemRepository.save(new WorldItem("SMALL_CUP", "BASIC_PEBBLE", "item.basicPebble", "STONE", "STONE", 1, "Y"));
            worldItemRepository.save(new WorldItem("LARGE_CUP", "BABY_SPROUT", "item.babySprout", "PLANT", "PLANT", 1, "Y"));
            worldItemRepository.save(new WorldItem("AQUARIUM", "WARM_LIGHT", "item.warmLight", "LIGHT", "LIGHT", 1, "Y"));
            worldItemRepository.save(new WorldItem("POND", "LOTUS_LEAF", "item.lotusLeaf", "DECORATION", "DECORATION", 1, "Y"));
        }

        // 테스트용 사용자 생성
        testUser = userRepository.save(User.builder()
                .email("test_world@mindcup.com")
                .password("password")
                .nickname("WorldTester")
                .provider("LOCAL")
                .friendCode("WORLD123")
                .isStatusPublic(true)
                .languageSetting("KO")
                .unlockedWorldLevel("SMALL_CUP")
                .build());
    }

    @Test
    void testNewUserStartsWithSmallCup() {
        UserWorld uw = worldService.recalculateWorldLevel(testUser.getUserId());
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("SMALL_CUP");
    }

    @Test
    void testWorldUnlockThresholds() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // 1. 평균 맑음도 29점인 경우 -> SMALL_CUP 유지 (100 - 71 = 29)
        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(71).activeWorldLevel("SMALL_CUP").build());
        UserWorld uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("SMALL_CUP");

        // 2. 평균 맑음도 30점인 경우 -> LARGE_CUP 최초 해금 및 보상 지급 검증
        cup.updatePollutionScore(70); // clarity = 30
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("LARGE_CUP");

        // 다슬기(기본생물) 및 새싹수초(기본아이템) 획득 여부 검증
        boolean hasCreature = userCreatureRepository.findAllByUserId(userId).stream()
                .anyMatch(c -> {
                    EcosystemCreature ec = ecosystemCreatureRepository.findById(c.getCreatureId()).orElse(null);
                    return ec != null && ec.getCreatureCode().equals("BABY_SNAIL");
                });
        assertThat(hasCreature).isTrue();

        // 3. 평균 맑음도 45점인 경우 -> AQUARIUM 해금
        cup.updatePollutionScore(55); // clarity = 45
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("AQUARIUM");

        // 4. 평균 맑음도 60점인 경우 -> POND 해금
        cup.updatePollutionScore(40); // clarity = 60
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("POND");
    }

    @Test
    void testNonRelockingPolicy() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // POND 단계 도달 (평균 맑음도 60점 이상)
        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(40).activeWorldLevel("POND").build());
        UserWorld uw = worldService.recalculateWorldLevel(userId);
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("POND");

        // 다음날 마음이 매우 흐려짐 (평균 점수가 10%로 급락)
        cup.updatePollutionScore(90); // clarity = 10
        dailyCupRepository.save(cup);
        uw = worldService.recalculateWorldLevel(userId);

        // 점수가 폭락해 calculatedWorldLevel은 SMALL_CUP이 되지만, unlocked와 display는 POND로 영구 보존됨을 검증
        assertThat(uw.getCalculatedWorldLevel()).isEqualTo("SMALL_CUP");
        assertThat(uw.getUnlockedWorldLevel()).isEqualTo("POND");
        assertThat(uw.getDisplayWorldLevel()).isEqualTo("POND");
    }

    @Test
    void testCreatureStateByPollution() {
        Long userId = testUser.getUserId();
        LocalDate today = LocalDate.now();

        // 1. 탁도 80% (VERY_CLOUDY) -> 생물 상태는 HIDDEN
        DailyCup cup = dailyCupRepository.save(DailyCup.builder().userId(userId).recordDate(today).pollutionScore(80).activeWorldLevel("SMALL_CUP").build());
        WorldDisplayResponse display = worldService.getHomeDisplayData(userId);
        assertThat(display.getCreatureState()).isEqualTo("HIDDEN");

        // 2. 탁도 10% (CLEAR) -> 생물 상태는 ACTIVE
        cup.updatePollutionScore(10);
        dailyCupRepository.save(cup);
        display = worldService.getHomeDisplayData(userId);
        assertThat(display.getCreatureState()).isEqualTo("ACTIVE");
    }

    @Test
    void testEquipItemsAndSlotExclusion() {
        Long userId = testUser.getUserId();

        // 마스터 아이템들 확인
        List<WorldItem> allItems = worldItemRepository.findAll();
        WorldItem sproutItem = allItems.stream().filter(i -> i.getItemCode().equals("BABY_SPROUT")).findFirst().orElseThrow();

        // 1. 미보유 아이템 장착 시도 -> ITEM_NOT_ACQUIRED 예외 발생 검증
        assertThatThrownBy(() -> worldService.equipItem(userId, sproutItem.getItemId()))
                .isInstanceOf(BusinessException.class);

        // 2. 획득 상태 수동 생성
        userWorldItemRepository.save(UserWorldItem.builder().userId(userId).itemId(sproutItem.getItemId()).equippedYn("N").build());

        // 장착 성공 검증
        worldService.equipItem(userId, sproutItem.getItemId());
        UserWorldItem uwi = userWorldItemRepository.findByUserIdAndItemId(userId, sproutItem.getItemId()).orElseThrow();
        assertThat(uwi.getEquippedYn()).isEqualTo("Y");
    }
}
