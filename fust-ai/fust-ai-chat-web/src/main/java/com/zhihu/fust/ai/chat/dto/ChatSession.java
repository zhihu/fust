package com.zhihu.fust.ai.chat.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话模型
 */
public class ChatSession {
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
     * 消息列表
     */
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 会话状态
     */
    private SessionStatus status = SessionStatus.ACTIVE;

    /**
     * 会话元数据
     */
    private SessionMetadata metadata;

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        ACTIVE,    // 活跃
        ARCHIVED,  // 归档
        DELETED    // 删除
    }

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

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public SessionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SessionMetadata metadata) {
        this.metadata = metadata;
    }
}