package com.zhihu.fust.ai.chat.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话元数据
 */
public class SessionMetadata {
    /**
     * 总消息数
     */
    private Integer messageCount = 0;

    /**
     * 用户消息数
     */
    private Integer userMessageCount = 0;

    /**
     * 助手消息数
     */
    private Integer assistantMessageCount = 0;

    /**
     * 会话标签
     */
    private Map<String, String> tags = new HashMap<>();

    /**
     * 扩展属性
     */
    private Map<String, Object> properties = new HashMap<>();

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getUserMessageCount() {
        return userMessageCount;
    }

    public void setUserMessageCount(Integer userMessageCount) {
        this.userMessageCount = userMessageCount;
    }

    public Integer getAssistantMessageCount() {
        return assistantMessageCount;
    }

    public void setAssistantMessageCount(Integer assistantMessageCount) {
        this.assistantMessageCount = assistantMessageCount;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}