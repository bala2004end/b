package com.aidb.assistant.mapper;

import com.aidb.assistant.dto.ChatMessageDTO;
import com.aidb.assistant.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {
    
    public ChatMessageDTO toDto(ChatMessage message) {
        if (message == null) return null;
        
        return new ChatMessageDTO(
                message.getId(),
                message.getConversation() != null ? message.getConversation().getId() : null,
                message.getSender(),
                message.getContent(),
                message.getGeneratedSql(),
                message.getChartType(),
                message.getResultJson(),
                message.getTimestamp()
        );
    }
}
