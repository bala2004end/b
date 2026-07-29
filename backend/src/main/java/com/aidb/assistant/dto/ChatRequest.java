package com.aidb.assistant.dto;

public class ChatRequest {
    private Long conversationId;
    private String question;
    private String geminiApiKey;

    public ChatRequest() {}
    public ChatRequest(Long conversationId, String question, String geminiApiKey) {
        this.conversationId = conversationId;
        this.question = question;
        this.geminiApiKey = geminiApiKey;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getGeminiApiKey() { return geminiApiKey; }
    public void setGeminiApiKey(String geminiApiKey) { this.geminiApiKey = geminiApiKey; }
}
