package com.mindcup.backend.domain.friend.service;

import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.friend.dto.FriendMindStatusResponse;
import com.mindcup.backend.domain.friend.entity.Friend;
import com.mindcup.backend.domain.friend.entity.UserFriendSetting;
import com.mindcup.backend.domain.friend.repository.FriendRepository;
import com.mindcup.backend.domain.friend.repository.UserFriendSettingRepository;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageRepository;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserFriendSettingRepository userFriendSettingRepository;
    private final UserRepository userRepository;
    private final DailyCupRepository dailyCupRepository;
    private final ComfortMessageRepository comfortMessageRepository;

    /**
     * 친구 코드로 친구 신청 전송
     */
    @Transactional
    public void sendFriendRequest(Long userId, String targetFriendCode) {
        User targetUser = userRepository.findByFriendCode(targetFriendCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Long targetUserId = targetUser.getUserId();

        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST); // 본인 신청 방지
        }

        // 차단 관계 시 신청 불가
        if (friendRepository.existsBlockedRelation(userId, targetUserId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        // 양방향 중복 신청 검증 (PENDING 또는 ACCEPTED가 이미 존재하면 중복 에러)
        if (friendRepository.existsPendingOrAcceptedRelation(userId, targetUserId)) {
            throw new BusinessException(ErrorCode.ACTION_DUPLICATE);
        }

        // 기존 REJECTED 관계가 있으면 리셋하여 PENDING으로 갱신
        Optional<Friend> exist = friendRepository.findRelation(userId, targetUserId);
        if (exist.isPresent()) {
            Friend f = exist.get();
            f.updateStatus("PENDING");
        } else {
            friendRepository.save(Friend.builder()
                    .userId(userId)
                    .targetUserId(targetUserId)
                    .status("PENDING")
                    .build());
        }
    }

    /**
     * 내 친구 요청 수락
     */
    @Transactional
    public void acceptFriendRequest(Long userId, Long requestId) {
        Friend f = friendRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!f.getTargetUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN); // 내 요청만 수락 가능
        }

        f.updateStatus("ACCEPTED");
    }

    /**
     * 내 친구 요청 거절
     */
    @Transactional
    public void rejectFriendRequest(Long userId, Long requestId) {
        Friend f = friendRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!f.getTargetUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        f.updateStatus("REJECTED");
    }

    /**
     * 친구 삭제 (ACCEPTED 파기)
     */
    @Transactional
    public void deleteFriend(Long userId, Long friendRelationId) {
        Friend f = friendRepository.findById(friendRelationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!f.getUserId().equals(userId) && !f.getTargetUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        friendRepository.delete(f);
    }

    /**
     * 친구 차단 (status -> BLOCKED 및 차단 주체 정보 영속화)
     */
    @Transactional
    public void blockFriend(Long userId, Long friendRelationId) {
        Friend f = friendRepository.findById(friendRelationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!f.getUserId().equals(userId) && !f.getTargetUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        f.block(userId);
    }

    /**
     * 나에게 온 친구 신청 PENDING 목록 조회
     */
    public List<Friend> getReceivedPendingRequests(Long userId) {
        return friendRepository.findAllByTargetUserIdAndStatus(userId, "PENDING");
    }

    /**
     * 친구 전체 마음 상태 목록 조회 (프라이버시 마스킹 연동)
     */
    public List<FriendMindStatusResponse> getFriendsMindStatusList(Long userId) {
        List<Friend> relations = friendRepository.findAllAcceptedFriends(userId);
        List<FriendMindStatusResponse> responseList = new ArrayList<>();

        for (Friend f : relations) {
            Long friendUserId = f.getUserId().equals(userId) ? f.getTargetUserId() : f.getUserId();
            responseList.add(getSingleFriendMindStatus(userId, friendUserId));
        }

        return responseList;
    }

    /**
     * 특정 친구의 마음 상태 단건 조회 (상호 차단 검증 및 공개범위 마스킹 연동)
     */
    public FriendMindStatusResponse getSingleFriendMindStatus(Long userId, Long friendUserId) {
        // 친구 관계 여부 및 차단 상태 검증
        Friend relation = friendRepository.findRelation(userId, friendUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTION_FORBIDDEN));

        if (!"ACCEPTED".equals(relation.getStatus())) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN); // 상호 친구 수락 상태가 아니면 403
        }

        if (friendRepository.existsBlockedRelation(userId, friendUserId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN); // 차단 관계 시 열람 제한
        }

        User friendInfo = userRepository.findById(friendUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 상대방 프라이버시 공개 레벨 로드
        UserFriendSetting setting = userFriendSettingRepository.findByUserId(friendUserId)
                .orElseGet(() -> userFriendSettingRepository.save(
                        UserFriendSetting.builder().userId(friendUserId).visibilityLevel("RECORD_STATUS_ONLY").build()
                ));

        String visibility = setting.getVisibilityLevel();

        // 상대방의 오늘 자 컵 획득
        LocalDate today = LocalDate.now();
        Optional<DailyCup> friendCupOpt = dailyCupRepository.findByUserIdAndRecordDate(friendUserId, today);

        boolean recordedToday = friendCupOpt.isPresent() && friendCupOpt.get().getPollutionScore() != 50;
        String mindCupLevel = friendCupOpt.map(DailyCup::getCupStatus).orElse("SLIGHTLY_CLOUDY");

        // 오늘 물방울 송신 제약 확인
        boolean alreadySent = comfortMessageRepository.existsBySenderUserIdAndReceiverUserIdAndSentDate(userId, friendUserId, today);
        boolean canSend = !alreadySent;

        // 마스킹 변수 필터 매핑
        Boolean responseRecorded = null;
        String responseCupLevel = null;
        String messageKey = "friend.status.private";

        if ("RECORD_STATUS_ONLY".equals(visibility)) {
            responseRecorded = recordedToday;
            messageKey = "friend.status.status_only";
        } else if ("CUP_LEVEL_ONLY".equals(visibility)) {
            responseRecorded = recordedToday;
            responseCupLevel = mindCupLevel;
            messageKey = "friend.status.level_only";
        }

        return FriendMindStatusResponse.builder()
                .friendUserId(friendUserId)
                .nickname(friendInfo.getNickname())
                .profileImageUrl(null) // 현재 아바타 미구현
                .visibilityLevel(visibility)
                .recordedToday(responseRecorded)
                .mindCupLevel(responseCupLevel)
                .canSendComfort(canSend)
                .alreadySentDropToday(alreadySent)
                .messageKey(messageKey)
                .build();
    }

    /**
     * 내 친구 설정 공개 범위 변경
     */
    @Transactional
    public void updateVisibilitySetting(Long userId, String newLevel) {
        if (!"PRIVATE".equals(newLevel) && !"RECORD_STATUS_ONLY".equals(newLevel) && !"CUP_LEVEL_ONLY".equals(newLevel)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        UserFriendSetting setting = userFriendSettingRepository.findByUserId(userId)
                .orElseGet(() -> userFriendSettingRepository.save(
                        UserFriendSetting.builder().userId(userId).visibilityLevel("RECORD_STATUS_ONLY").build()
                ));

        setting.updateVisibilityLevel(newLevel);
    }

    public String getMyVisibilitySetting(Long userId) {
        UserFriendSetting setting = userFriendSettingRepository.findByUserId(userId)
                .orElseGet(() -> userFriendSettingRepository.save(
                        UserFriendSetting.builder().userId(userId).visibilityLevel("RECORD_STATUS_ONLY").build()
                ));
        return setting.getVisibilityLevel();
    }
}
