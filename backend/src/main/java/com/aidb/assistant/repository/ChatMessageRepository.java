package com.aidb.assistant.repository;

import com.aidb.assistant.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);
    List<ChatMessage> findTop20ByGeneratedSqlIsNotNullOrderByTimestampDesc();
}
