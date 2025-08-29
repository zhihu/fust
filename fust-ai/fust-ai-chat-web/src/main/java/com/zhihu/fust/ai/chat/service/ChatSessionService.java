package com.zhihu.fust.ai.chat.service;

import com.zhihu.fust.ai.chat.dao.UserChatSessionDao;
import com.zhihu.fust.ai.chat.dto.ChatMessage;
import com.zhihu.fust.ai.chat.dto.CreateSessionRequest;
import com.zhihu.fust.ai.chat.dto.SessionDetailResponse;
import com.zhihu.fust.ai.chat.dto.SessionResponse;
import com.zhihu.fust.ai.chat.model.UserChatSessionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * 基于 Spring AI 的会话管理服务
 * 管理会话元数据，聊天记忆由 Spring AI ChatMemory 自动管理
 */
@Service
public class ChatSessionService {

    private static final Logger log = LoggerFactory.getLogger(ChatSessionService.class);

    private final UserChatSessionDao userChatSessionDao;
    private final ChatService chatService;
    private final ChatHistoryService chatHistoryService;

    public ChatSessionService(UserChatSessionDao userChatSessionDao, ChatService chatService, ChatHistoryService chatHistoryService) {
        this.userChatSessionDao = userChatSessionDao;
        this.chatService = chatService;
        this.chatHistoryService = chatHistoryService;
    }

    /**
     * 创建新会话
     */
    public SessionResponse createSession(CreateSessionRequest request) {
        String sessionId = generateSessionId();
        Instant now = Instant.now();

        UserChatSessionModel sessionModel = new UserChatSessionModel();
        sessionModel.setConversationId(sessionId);
        sessionModel.setTitle(request.getTitle() != null ? request.getTitle() : "新对话");
        sessionModel.setUserId("default"); // 暂时使用默认用户ID
        sessionModel.setCreatedAt(now);
        sessionModel.setUpdatedAt(now);

        // 保存到数据库
        userChatSessionDao.create(sessionModel);

        log.info("创建新会话: {}, 标题: {}", sessionId, sessionModel.getTitle());

        return convertToSessionResponse(sessionModel);
    }

    /**
     * 获取所有会话列表
     */
    public List<SessionResponse> getSessions() {
        try {
            List<UserChatSessionModel> sessions = userChatSessionDao.findAll();
            return sessions.stream()
                    .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                    .map(this::convertToSessionResponse)
                    .toList();
        } catch (Exception e) {
            log.error("获取会话列表失败", e);
            throw new RuntimeException("获取会话列表失败", e);
        }
    }

    /**
     * 获取会话详情
     */
    public SessionDetailResponse getSessionDetail(String sessionId) {
        try {
            UserChatSessionModel session = userChatSessionDao.findByConversationId(sessionId);
            if (session == null) {
                throw new RuntimeException("会话不存在: " + sessionId);
            }

            // 获取聊天历史（从 Spring AI ChatMemory）
            var messages = chatHistoryService.getConversationHistory(sessionId);

            return convertToSessionDetailResponse(session, messages);
        } catch (Exception e) {
            log.error("获取会话详情失败，会话ID: {}", sessionId, e);
            throw new RuntimeException("获取会话详情失败", e);
        }
    }

    /**
     * 检查会话是否存在
     */
    public boolean sessionExists(String sessionId) {
        try {
            UserChatSessionModel session = userChatSessionDao.findByConversationId(sessionId);
            return session != null;
        } catch (Exception e) {
            log.error("检查会话是否存在失败，会话ID: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) {
        try {
            // 删除会话元数据
            userChatSessionDao.deleteByConversationId(sessionId);

            // 删除聊天记忆
            chatService.deleteConversation(sessionId);

            log.info("删除会话: {}", sessionId);
        } catch (Exception e) {
            log.error("删除会话失败，会话ID: {}", sessionId, e);
            throw new RuntimeException("删除会话失败", e);
        }
    }

    /**
     * 更新会话标题
     */
    public void updateSessionTitle(String sessionId, String title) {
        try {
            Instant now = Instant.now();
            int updated = userChatSessionDao.updateByConversationId(sessionId, title, now);
            if (updated == 0) {
                throw new RuntimeException("会话不存在: " + sessionId);
            }

            log.info("更新会话标题: {} -> {}", sessionId, title);
        } catch (Exception e) {
            log.error("更新会话标题失败，会话ID: {}", sessionId, e);
            throw new RuntimeException("更新会话标题失败", e);
        }
    }

    /**
     * 更新会话的最后活动时间
     */
    public void updateSessionActivity(String sessionId) {
        try {
            UserChatSessionModel session = userChatSessionDao.findByConversationId(sessionId);
            if (session != null) {
                Instant now = Instant.now();
                userChatSessionDao.updateByConversationId(sessionId, session.getTitle(), now);
            }
        } catch (Exception e) {
            log.warn("更新会话活动时间失败，会话ID: {}", sessionId, e);
            // 这个失败不应该影响主流程
        }
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 转换为会话响应DTO
     */
    private SessionResponse convertToSessionResponse(UserChatSessionModel session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getConversationId()); // 使用conversationId作为前端显示的ID
        response.setTitle(session.getTitle());
        // 因为UserChatSessionModel中没有description字段，先设置为null
        response.setDescription(null);
        response.setCreateTime(instantToLocalDateTime(session.getCreatedAt()));
        response.setUpdateTime(instantToLocalDateTime(session.getUpdatedAt()));

        // 获取消息数量
        try {
            List<Message> messages = chatHistoryService.getConversationHistory(session.getConversationId());
            response.setMessageCount(messages.size());

            // 设置最后一条消息
            if (!messages.isEmpty()) {
                Message lastMessage = messages.get(messages.size() - 1);
                ChatMessage lastChatMessage = new ChatMessage();
                lastChatMessage.setContent(lastMessage.getText());
                lastChatMessage.setType(lastMessage.getMessageType());
                lastChatMessage.setCreateTime(instantToLocalDateTime(session.getUpdatedAt()));
                response.setLastMessage(lastChatMessage);
            }
        } catch (Exception e) {
            log.warn("获取会话消息信息失败，会话ID: {}", session.getConversationId(), e);
            response.setMessageCount(0);
        }

        return response;
    }

    /**
     * 转换为会话详情响应DTO
     */
    private SessionDetailResponse convertToSessionDetailResponse(UserChatSessionModel session, List<Message> messages) {
        SessionDetailResponse response = new SessionDetailResponse();
        response.setId(session.getConversationId());
        response.setTitle(session.getTitle());
        response.setDescription(null); // UserChatSessionModel中没有description字段
        response.setCreateTime(instantToLocalDateTime(session.getCreatedAt()));
        response.setUpdateTime(instantToLocalDateTime(session.getUpdatedAt()));

        // 转换消息格式
        List<ChatMessage> chatMessages = messages.stream()
                .map(msg -> {
                    var chatMessage = new ChatMessage();
                    chatMessage.setId(UUID.randomUUID().toString()); // 生成临时ID用于前端显示
                    chatMessage.setSessionId(session.getConversationId());
                    chatMessage.setType(msg.getMessageType());
                    chatMessage.setContent(msg.getText());
                    chatMessage.setCreateTime(instantToLocalDateTime(session.getUpdatedAt()));
                    return chatMessage;
                }) // 使用会话更新时间，实际应该有消息时间戳
                .toList();

        response.setMessages(chatMessages);

        return response;
    }

    /**
     * 将 Instant 转换为 LocalDateTime
     */
    private LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
} 