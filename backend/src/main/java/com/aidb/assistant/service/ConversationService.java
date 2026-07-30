package com.aidb.assistant.service;

import com.aidb.assistant.entity.Conversation;
import com.aidb.assistant.entity.User;
import com.aidb.assistant.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;

    @Transactional
    public Conversation getOrCreateConversation(Long conversationId, String initialQuestion, User user) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseGet(() -> createNewConversation(initialQuestion, user));
        }
        return createNewConversation(initialQuestion, user);
    }

    private Conversation createNewConversation(String initialQuestion, User user) {
        String title = initialQuestion.length() > 30 ? initialQuestion.substring(0, 30) + "..." : initialQuestion;
        Conversation conversation = Conversation.builder()
                .title(title)
                .user(user)
                .build();
        return conversationRepository.save(conversation);
    }
}
