package com.zhihu.fust.ai.chat.dto;

import java.time.LocalDateTime;

/**
 * 会话响应DTO
 */
public class SessionResponse {
    /**
     * 会话ID
     */
    private String id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 会话状态
     */
    private ChatSession.SessionStatus status;

    /**
     * 会话元数据
     */
    private SessionMetadata metadata;

    /**
     * 最后一条消息（用于列表显示）
     */
    private ChatMessage lastMessage;

    /**
     * 消息数量
     */
    private Integer messageCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public ChatSession.SessionStatus getStatus() {
        return status;
    }

    public void setStatus(ChatSession.SessionStatus status) {
        this.status = status;
    }

    public SessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SessionMetadata metadata) {
        this.metadata = metadata;
    }

    public ChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}