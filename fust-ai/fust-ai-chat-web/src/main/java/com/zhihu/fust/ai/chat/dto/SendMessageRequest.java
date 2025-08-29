package com.zhihu.fust.ai.chat.dto;

/**
 * 发送消息请求DTO
 */

public class SendMessageRequest {
    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String message;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}