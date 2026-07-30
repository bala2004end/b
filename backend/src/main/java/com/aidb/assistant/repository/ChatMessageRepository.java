package com.aidb.assistant.repository;

import com.aidb.assistant.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationIdOrderByTimestampAsc(Long conversationId);

    /**
     * Returns the 20 most recent SQL messages for a specific user — scoped by username
     * through the conversation -> user relationship.
     */
    List<ChatMessage> findTop20ByGeneratedSqlIsNotNullAndConversation_User_UsernameOrderByTimestampDesc(String username);
}
