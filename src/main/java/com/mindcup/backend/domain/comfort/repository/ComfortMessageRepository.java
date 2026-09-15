package com.mindcup.backend.domain.comfort.repository;

import com.mindcup.backend.domain.comfort.entity.ComfortMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ComfortMessageRepository extends JpaRepository<ComfortMessage, Long> {
    
    boolean existsBySenderUserIdAndReceiverUserIdAndSentDate(Long senderUserId, Long receiverUserId, LocalDate sentDate);
    
    List<ComfortMessage> findAllByReceiverUserIdOrderByCreatedAtDesc(Long receiverUserId);

    long countBySenderUserIdAndSentDateBetween(Long senderUserId, LocalDate start, LocalDate end);

    long countByReceiverUserIdAndPouredYnAndSentDateBetween(Long receiverUserId, String pouredYn, LocalDate start, LocalDate end);
}
