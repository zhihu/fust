package com.zhihu.fust.ai.chat.service;

import com.zhihu.fust.ai.chat.dao.ChatHistoryDao;
import com.zhihu.fust.ai.chat.model.ChatHistoryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatHistoryService {
    private static final Logger log = LoggerFactory.getLogger(ChatHistoryService.class);
    private final ChatHistoryDao chatHistoryDao;

    public ChatHistoryService(ChatHistoryDao chatHistoryDao) {
        this.chatHistoryDao = chatHistoryDao;
    }

    /**
     * 获取会话的消息历史
     *
     * @param conversationId 会话ID
     * @return 消息历史
     */
    public List<Message> getConversationHistory(String conversationId) {
        try {
            var messages = chatHistoryDao.findByConversationId(conversationId);
            return messages.stream()
                    .map(this::deserializeMessage)
                    .toList();

        } catch (Exception e) {
            log.error("获取会话历史失败，会话ID: {}", conversationId, e);
            return List.of();
        }
    }

    /**
     * 获取会话的消息历史
     *
     * @param conversationId 会话ID
     * @return 消息历史
     */
    public List<Message> getConversationForChatMemory(String conversationId, int maxSize) {
        try {
            var models = chatHistoryDao.findByConversationIdWithLimit(conversationId, maxSize);
            return models.stream()
                    .map(this::deserializeMessage)
                    .toList();

        } catch (Exception e) {
            log.error("获取会话历史失败，会话ID: {}", conversationId, e);
            return List.of();
        }
    }

    @Transactional
    public void saveMessage(MessageType type, String conversationId, String message) {
        ChatHistoryModel model = new ChatHistoryModel();
        model.setContent(message);
        model.setType(type.name());
        model.setMetadata("");
        model.setConversationId(conversationId);
        chatHistoryDao.create(model);
    }

    /**
     * 将ChatMemoryModel反序列化为Message
     */
    private Message deserializeMessage(ChatHistoryModel model) {
        try {
            MessageType messageType = MessageType.valueOf(model.getType());

            String content = model.getContent();
            // 根据消息类型创建对应的Message实例
            switch (messageType) {
                case USER:
                    return new UserMessage(content);
                case ASSISTANT:
                    return new AssistantMessage(content);
                case SYSTEM:
                    return new SystemMessage(content);
                default:
                    log.warn("未知的消息类型: {}", messageType);
                    return new UserMessage(content);
            }

        } catch (Exception e) {
            log.error("反序列化消息失败，消息ID: {}", model.getId(), e);
            return null;
        }
    }

    public int deleteByConversationId(String conversationId) {
        return chatHistoryDao.deleteByConversationId(conversationId);
    }
}
