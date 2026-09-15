package com.mindcup.backend.domain.comfort.service;

import com.mindcup.backend.domain.comfort.dto.ComfortMessageTemplateResponse;
import com.mindcup.backend.domain.comfort.entity.ComfortMessage;
import com.mindcup.backend.domain.comfort.entity.ComfortMessageTemplate;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageRepository;
import com.mindcup.backend.domain.comfort.repository.ComfortMessageTemplateRepository;
import com.mindcup.backend.domain.cup.entity.DailyCup;
import com.mindcup.backend.domain.cup.entity.MindScoreEffectLog;
import com.mindcup.backend.domain.cup.repository.DailyCupRepository;
import com.mindcup.backend.domain.cup.repository.MindScoreEffectLogRepository;
import com.mindcup.backend.domain.friend.repository.FriendRepository;
import com.mindcup.backend.domain.world.service.WorldService;
import com.mindcup.backend.domain.user.entity.User;
import com.mindcup.backend.domain.user.repository.UserRepository;
import com.mindcup.backend.global.exception.BusinessException;
import com.mindcup.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComfortService {

    private final ComfortMessageTemplateRepository templateRepository;
    private final ComfortMessageRepository comfortMessageRepository;
    private final FriendRepository friendRepository;
    private final DailyCupRepository dailyCupRepository;
    private final MindScoreEffectLogRepository effectLogRepository;
    private final WorldService worldService;
    private final UserRepository userRepository;

    /**
     * 로케일에 따른 위로 템플릿 목록 조회
     */
    public List<ComfortMessageTemplateResponse> getTemplates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        String lang = user.getLanguageSetting(); // KO or EN
        List<ComfortMessageTemplate> templates = templateRepository.findAll();
        List<ComfortMessageTemplateResponse> responseList = new ArrayList<>();

        for (ComfortMessageTemplate t : templates) {
            String content = "KO".equalsIgnoreCase(lang) ? t.getContentKo() : t.getContentEn();
            responseList.add(new ComfortMessageTemplateResponse(t.getTemplateId(), content));
        }

        return responseList;
    }

    /**
     * 친구에게 한 방울 전송 (송신 시 일일 한도 -10 절대치 산출, recalculate 연동)
     */
    @Transactional
    public void sendComfortMessage(Long senderId, Long receiverId, Long templateId) {
        // 친구 관계 검증 (ACCEPTED 이고 BLOCKED가 아니어야 함)
        boolean isFriend = friendRepository.findRelation(senderId, receiverId)
                .map(f -> "ACCEPTED".equals(f.getStatus())).orElse(false);

        if (!isFriend || friendRepository.existsBlockedRelation(senderId, receiverId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        LocalDate today = LocalDate.now();

        // 하루 1회 송신 제한 검증
        boolean alreadySent = comfortMessageRepository
                .existsBySenderUserIdAndReceiverUserIdAndSentDate(senderId, receiverId, today);
        if (alreadySent) {
            // 중복 발송 에러 반환
            throw new BusinessException(ErrorCode.ACTION_DUPLICATE);
        }

        // 일일 차감 한도 계산 (abs(sum(effectScore)) 기준)
        int sumScore = effectLogRepository.sumEffectScoreByDateAndType(senderId, today, "COMFORT_SENT");
        int alreadyUsed = Math.abs(sumScore);
        int remaining = 10 - alreadyUsed;

        int effectScore = 0;
        String limitExceeded = "Y";

        if (remaining >= 2) {
            effectScore = -2;
            limitExceeded = "N";

            // 내 오늘 자 마음컵 맑게 갱신
            DailyCup senderCup = dailyCupRepository.findByUserIdAndRecordDate(senderId, today)
                    .orElseGet(() -> dailyCupRepository.save(DailyCup.builder()
                            .userId(senderId)
                            .recordDate(today)
                            .pollutionScore(50)
                            .activeWorldLevel("SMALL_CUP")
                            .build()));
            senderCup.updatePollutionScore(senderCup.getPollutionScore() - 2);
            dailyCupRepository.save(senderCup);
        }

        // 메시지 생성 및 보관함 저장
        ComfortMessage msg = comfortMessageRepository.save(ComfortMessage.builder()
                .senderUserId(senderId)
                .receiverUserId(receiverId)
                .templateId(templateId)
                .senderEffectScore(effectScore)
                .receiverEffectScore(3) // 붓기 시 기본 효과 -3
                .build());

        // 점수 변경 로그 기록 (sourceId로 comfortId 바인딩)
        effectLogRepository.save(MindScoreEffectLog.builder()
                .userId(senderId)
                .sourceType("COMFORT_SENT")
                .sourceId(msg.getComfortId())
                .effectScore(effectScore)
                .effectDate(today)
                .limitExceededYn(limitExceeded)
                .build());

        // E2E 단일 트랜잭션 하에 세계 성장 동기 업데이트
        worldService.recalculateWorldLevel(senderId);
    }

    /**
     * 내가 받은 한 방울 목록 조회
     */
    public List<ComfortMessage> getReceivedMessages(Long receiverId) {
        return comfortMessageRepository.findAllByReceiverUserIdOrderByCreatedAtDesc(receiverId);
    }

    /**
     * 받은 한 방울 읽음 처리
     */
    @Transactional
    public void readComfortMessage(Long receiverId, Long comfortId) {
        ComfortMessage msg = comfortMessageRepository.findById(comfortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!msg.getReceiverUserId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN);
        }

        msg.markAsRead();
    }

    /**
     * 받은 한 방울 내 컵에 붓기 (붓기 일일 한도 -15 절대치 검증, recalculate 연동)
     */
    @Transactional
    public void pourComfortMessage(Long receiverId, Long comfortId) {
        ComfortMessage msg = comfortMessageRepository.findById(comfortId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (!msg.getReceiverUserId().equals(receiverId)) {
            throw new BusinessException(ErrorCode.ACTION_FORBIDDEN); // 타인의 물방울 도용 방지
        }

        if ("Y".equals(msg.getPouredYn())) {
            throw new BusinessException(ErrorCode.CUP_ALREADY_CHECKED); // 중복 붓기 방지
        }

        LocalDate today = LocalDate.now();

        // 일일 붓기 누적 한도 판정
        int sumScore = effectLogRepository.sumEffectScoreByDateAndType(receiverId, today, "COMFORT_POURED");
        int alreadyUsed = Math.abs(sumScore);

        if (alreadyUsed >= 15) {
            // 붓기 한도 도달 시 poured_yn = 'N' 상태를 그대로 두고 409 Conflict 커스텀 비즈니스 예외 투척
            throw new BusinessException(ErrorCode.POUR_LIMIT_EXCEEDED);
        }

        // 붓기 적용 및 점수 차감
        msg.pour();
        comfortMessageRepository.save(msg);

        DailyCup receiverCup = dailyCupRepository.findByUserIdAndRecordDate(receiverId, today)
                .orElseGet(() -> dailyCupRepository.save(DailyCup.builder()
                        .userId(receiverId)
                        .recordDate(today)
                        .pollutionScore(50)
                        .activeWorldLevel("SMALL_CUP")
                        .build()));
        
        receiverCup.updatePollutionScore(receiverCup.getPollutionScore() - 3);
        dailyCupRepository.save(receiverCup);

        // 점수 변경 로그 기록
        effectLogRepository.save(MindScoreEffectLog.builder()
                .userId(receiverId)
                .sourceType("COMFORT_POURED")
                .sourceId(comfortId)
                .effectScore(-3)
                .effectDate(today)
                .limitExceededYn("N")
                .build());

        // E2E 단일 트랜잭션 하에 세계 성장 동기 업데이트
        worldService.recalculateWorldLevel(receiverId);
    }
}
