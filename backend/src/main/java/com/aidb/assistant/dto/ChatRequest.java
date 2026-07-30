package com.aidb.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private Long conversationId;

    @NotBlank(message = "Question is required")
    private String question;
    private String geminiApiKey;
}
