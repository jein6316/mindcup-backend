package com.mindcup.backend.domain.world.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.world.dto.WorldDetailResponse;
import com.mindcup.backend.domain.world.dto.WorldDisplayResponse;
import com.mindcup.backend.domain.world.entity.EcosystemCreature;
import com.mindcup.backend.domain.world.entity.UserCreature;
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
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorldService {

    private final DailyCupRepository dailyCupRepository;
    private final WorldLevelRepository worldLevelRepository;
    private final UserWorldRepository userWorldRepository;
    private final EcosystemCreatureRepository ecosystemCreatureRepository;
    private final UserCreatureRepository userCreatureRepository;
    private final WorldItemRepository worldItemRepository;
    private final UserWorldItemRepository userWorldItemRepository;

    /**
     * 최근 7일 평균 맑음도 계산 (기록이 있는 날짜만 평균 산출, 신규 유저는 0.0 반환)
     */
    public double calculateAverageClarityScore(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);

        List<DailyCup> existCups = dailyCupRepository
                .findAllByUserIdAndRecordDateBetweenOrderByRecordDateAsc(userId, start, today);

        if (existCups.isEmpty()) {
            return 0.0;
        }

        double totalClarity = 0.0;
        for (DailyCup cup : existCups) {
            totalClarity += (100 - cup.getPollutionScore());
        }

        return totalClarity / existCups.size();
    }

    /**
     * 세계 해금 상태 갱신 (점수가 하락하더라도 기존 최고 unlocked 레벨 보존 정책)
     */
    @Transactional
    public UserWorld recalculateWorldLevel(Long userId) {
        double avgClarity = calculateAverageClarityScore(userId);
        int avgInt = (int) Math.round(avgClarity);

        // 전체 활성 월드 레벨 기준치 조회
        List<WorldLevel> activeLevels = worldLevelRepository.findAll().stream()
                .filter(l -> "Y".equals(l.getUseYn()))
                .sorted(Comparator.comparingInt(WorldLevel::getSortOrder))
                .toList();

        // 7일 평균 점수로 도달 가능한 레벨 (calculatedWorldLevel) 판별
        WorldLevel calculated;
        if (activeLevels.isEmpty()) {
            calculated = new WorldLevel("SMALL_CUP", "world.smallCup", 0, 1, "Y");
        } else {
            calculated = activeLevels.get(0); // 기본 시작 SMALL_CUP
            for (WorldLevel wl : activeLevels) {
                if (avgInt >= wl.getRequiredAverageClarityScore()) {
                    calculated = wl;
                }
            }
        }

        // 기존 저장된 사용자 세계 로드 또는 SMALL_CUP 기본 빌드
        UserWorld userWorld = userWorldRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserWorld nw = UserWorld.builder()
                            .userId(userId)
                            .unlockedWorldLevel("SMALL_CUP")
                            .displayWorldLevel("SMALL_CUP")
                            .calculatedWorldLevel("SMALL_CUP")
                            .lastCalculatedClarityScore(0)
                            .build();
                    UserWorld saved = userWorldRepository.save(nw);
                    // 최초 기본 보상 지급
                    grantDefaultRewards(userId, "SMALL_CUP");
                    return saved;
                });

        // 최고 해금 레벨 판단 (영구 해금 정책: sortOrder 비교)
        WorldLevel currentUnlocked = worldLevelRepository.findByWorldLevelCode(userWorld.getUnlockedWorldLevel())
                .orElseGet(() -> new WorldLevel(userWorld.getUnlockedWorldLevel(), "world.smallCup", 0, 1, "Y"));

        String newUnlockedCode = userWorld.getUnlockedWorldLevel();
        if (calculated.getSortOrder() > currentUnlocked.getSortOrder()) {
            newUnlockedCode = calculated.getWorldLevelCode();
            // 등급 점프 시 누락 보상 방지를 위해 중간의 모든 레벨에 대한 기본 보상을 순차 지급
            for (WorldLevel wl : activeLevels) {
                if (wl.getSortOrder() > currentUnlocked.getSortOrder() && wl.getSortOrder() <= calculated.getSortOrder()) {
                    grantDefaultRewards(userId, wl.getWorldLevelCode());
                }
            }
        }

        // displayWorldLevel = unlockedWorldLevel 적용 (영구 해금 세계를 기본 시각화)
        userWorld.updateWorldLevels(newUnlockedCode, newUnlockedCode, calculated.getWorldLevelCode(), avgInt);
        return userWorld;
    }

    /**
     * 특정 세계 해금 시 기본 생물 및 슬롯 아이템 자동 보상 지급
     */
    private void grantDefaultRewards(Long userId, String worldLevelCode) {
        // 1. 기본 노출 생물 지급 (default_visible_yn = 'Y')
        List<EcosystemCreature> creatures = ecosystemCreatureRepository.findAllByWorldLevelCodeAndUseYn(worldLevelCode, "Y");
        for (EcosystemCreature ec : creatures) {
            if ("Y".equals(ec.getDefaultVisibleYn())) {
                if (!userCreatureRepository.existsByUserIdAndCreatureId(userId, ec.getCreatureId())) {
                    userCreatureRepository.save(UserCreature.builder()
                            .userId(userId)
                            .creatureId(ec.getCreatureId())
                            .build());
                }
            }
        }

        // 2. 기본 조경 아이템 지급 및 자동 장착
        List<WorldItem> items = worldItemRepository.findAllByWorldLevelCodeAndUseYn(worldLevelCode, "Y");
        for (WorldItem wi : items) {
            if (!userWorldItemRepository.existsByUserIdAndItemId(userId, wi.getItemId())) {
                // 기본으로 획득한 것은 자동 장착(Y) 처리
                userWorldItemRepository.save(UserWorldItem.builder()
                        .userId(userId)
                        .itemId(wi.getItemId())
                        .equippedYn("Y")
                        .build());
            }
        }
    }

    /**
     * 내 세계 상세 정보 조회
     */
    public WorldDetailResponse getMyWorldDetail(Long userId) {
        UserWorld uw = userWorldRepository.findByUserId(userId)
                .orElseGet(() -> recalculateWorldLevel(userId));

        double avg = calculateAverageClarityScore(userId);

        // 다음 해금 레벨 타겟 획득
        List<WorldLevel> activeLevels = worldLevelRepository.findAll().stream()
                .filter(l -> "Y".equals(l.getUseYn()))
                .sorted(Comparator.comparingInt(WorldLevel::getSortOrder))
                .toList();

        WorldLevel currentUnlocked = worldLevelRepository.findByWorldLevelCode(uw.getUnlockedWorldLevel())
                .orElseGet(() -> new WorldLevel(uw.getUnlockedWorldLevel(), "world.smallCup", 0, 1, "Y"));

        String nextCode = null;
        Integer nextReq = null;

        for (WorldLevel wl : activeLevels) {
            if (wl.getSortOrder() == currentUnlocked.getSortOrder() + 1) {
                nextCode = wl.getWorldLevelCode();
                nextReq = wl.getRequiredAverageClarityScore();
                break;
            }
        }

        return WorldDetailResponse.builder()
                .unlockedWorldLevel(uw.getUnlockedWorldLevel())
                .displayWorldLevel(uw.getDisplayWorldLevel())
                .averageClarityScore(avg)
                .nextWorldLevel(nextCode)
                .nextRequiredClarityScore(nextReq)
                .build();
    }

    /**
     * 조경 아이템 장착 (슬롯별 1개 제한 정책)
     */
    @Transactional
    public void equipItem(Long userId, Long itemId) {
        // 보유 여부 검증
        UserWorldItem userItem = userWorldItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_ACQUIRED));

        WorldItem targetItem = worldItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 동일 슬롯 타입(slotType)을 가진 다른 장착 아이템 해제 처리
        List<UserWorldItem> equipped = userWorldItemRepository.findAllByUserIdAndEquippedYn(userId, "Y");
        for (UserWorldItem eq : equipped) {
            WorldItem eqMeta = worldItemRepository.findById(eq.getItemId()).orElse(null);
            if (eqMeta != null && eqMeta.getSlotType().equalsIgnoreCase(targetItem.getSlotType())) {
                eq.updateEquippedStatus("N");
            }
        }

        userItem.updateEquippedStatus("Y");
    }

    /**
     * 조경 아이템 해제
     */
    @Transactional
    public void unequipItem(Long userId, Long itemId) {
        UserWorldItem userItem = userWorldItemRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_ACQUIRED));

        userItem.updateEquippedStatus("N");
    }

    /**
     * 홈 화면 렌더링용 통합 디스플레이 데이터 조회
     */
    public WorldDisplayResponse getHomeDisplayData(Long userId) {
        UserWorld uw = userWorldRepository.findByUserId(userId)
                .orElseGet(() -> recalculateWorldLevel(userId));

        double avg = calculateAverageClarityScore(userId);

        // 다음 레벨 정보 구하기
        List<WorldLevel> activeLevels = worldLevelRepository.findAll().stream()
                .filter(l -> "Y".equals(l.getUseYn()))
                .sorted(Comparator.comparingInt(WorldLevel::getSortOrder))
                .toList();

        WorldLevel currentUnlocked = worldLevelRepository.findByWorldLevelCode(uw.getUnlockedWorldLevel())
                .orElseGet(() -> new WorldLevel(uw.getUnlockedWorldLevel(), "world.smallCup", 0, 1, "Y"));

        String nextCode = null;
        Integer nextReq = null;
        for (WorldLevel wl : activeLevels) {
            if (wl.getSortOrder() == currentUnlocked.getSortOrder() + 1) {
                nextCode = wl.getWorldLevelCode();
                nextReq = wl.getRequiredAverageClarityScore();
                break;
            }
        }

        // 오늘 자 마음컵 조회 (오늘 기록이 전혀 없어도 디폴트로 가동되게 방어)
        LocalDate today = LocalDate.now();
        Optional<DailyCup> todayCupOpt = dailyCupRepository.findByUserIdAndRecordDate(userId, today);
        int pollution = todayCupOpt.map(DailyCup::getPollutionScore).orElse(50);
        int clarity = 100 - pollution;

        // 마음컵 등급 및 생태계 생물 휴식상태 판정
        String mindCupLevel;
        String creatureState;
        String messageKey;

        if (pollution <= 20) {
            mindCupLevel = "CLEAR";
            creatureState = "ACTIVE";
            messageKey = "creature.active";
        } else if (pollution <= 40) {
            mindCupLevel = "SLIGHTLY_CLOUDY";
            creatureState = "SLOW";
            messageKey = "creature.slow";
        } else if (pollution <= 70) {
            mindCupLevel = "CLOUDY";
            creatureState = "RESTING";
            messageKey = "creature.resting";
        } else {
            mindCupLevel = "VERY_CLOUDY";
            creatureState = "HIDDEN";
            messageKey = "creature.hidden";
        }

        // 사용자가 획득한 생물들 중 현재 displayWorldLevel에 매핑되는 생물 목록 가공
        List<UserCreature> myCreatures = userCreatureRepository.findAllByUserId(userId);
        List<WorldDisplayResponse.CreatureDto> creaturesDto = new ArrayList<>();
        for (UserCreature mc : myCreatures) {
            EcosystemCreature ec = ecosystemCreatureRepository.findById(mc.getCreatureId()).orElse(null);
            if (ec != null && ec.getWorldLevelCode().equals(uw.getDisplayWorldLevel())) {
                creaturesDto.add(WorldDisplayResponse.CreatureDto.builder()
                        .creatureCode(ec.getCreatureCode())
                        .creatureName(ec.getCreatureNameKey()) // 화면에서 i18n 렌더링에 적합하도록 키 매핑
                        .creatureType(ec.getCreatureType())
                        .defaultVisibleYn(ec.getDefaultVisibleYn())
                        .build());
            }
        }

        // 사용자가 장착한 꾸미기 아이템 목록 가공
        List<UserWorldItem> myEquippedItems = userWorldItemRepository.findAllByUserIdAndEquippedYn(userId, "Y");
        List<WorldDisplayResponse.ItemDto> itemsDto = new ArrayList<>();
        for (UserWorldItem uwi : myEquippedItems) {
            WorldItem wi = worldItemRepository.findById(uwi.getItemId()).orElse(null);
            if (wi != null) {
                itemsDto.add(WorldDisplayResponse.ItemDto.builder()
                        .itemCode(wi.getItemCode())
                        .itemName(wi.getItemNameKey())
                        .itemType(wi.getItemType())
                        .slotType(wi.getSlotType())
                        .build());
            }
        }

        return WorldDisplayResponse.builder()
                .unlockedWorldLevel(uw.getUnlockedWorldLevel())
                .displayWorldLevel(uw.getDisplayWorldLevel())
                .nextWorldLevel(nextCode)
                .averageClarityScore(avg)
                .nextRequiredClarityScore(nextReq)
                .pollutionScore(pollution)
                .clarityScore(clarity)
                .mindCupLevel(mindCupLevel)
                .creatureState(creatureState)
                .messageKey(messageKey)
                .creatures(creaturesDto)
                .equippedItems(itemsDto)
                .build();
    }

    /**
     * 전체 생물 목록 조회 (도감용)
     */
    public List<EcosystemCreature> getAllCreatures() {
        return ecosystemCreatureRepository.findAll().stream()
                .filter(c -> "Y".equals(c.getUseYn()))
                .toList();
    }

    /**
     * 내가 수집 완료한 생물 목록 조회
     */
    public List<EcosystemCreature> getMyAcquiredCreatures(Long userId) {
        List<UserCreature> my = userCreatureRepository.findAllByUserId(userId);
        List<EcosystemCreature> result = new ArrayList<>();
        for (UserCreature uc : my) {
            ecosystemCreatureRepository.findById(uc.getCreatureId())
                    .filter(c -> "Y".equals(c.getUseYn()))
                    .ifPresent(result::add);
        }
        return result;
    }

    /**
     * 전체 꾸미기 아이템 목록 조회
     */
    public List<WorldItem> getAllWorldItems() {
        return worldItemRepository.findAll().stream()
                .filter(i -> "Y".equals(i.getUseYn()))
                .toList();
    }

    /**
     * 내가 획득/보유 중인 꾸미기 아이템 목록 조회
     */
    public List<UserWorldItem> getMyWorldItems(Long userId) {
        return userWorldItemRepository.findAllByUserId(userId);
    }
}
