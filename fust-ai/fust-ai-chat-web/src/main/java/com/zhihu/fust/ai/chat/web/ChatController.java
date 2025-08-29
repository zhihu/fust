package com.zhihu.fust.ai.chat.web;

import com.zhihu.fust.ai.chat.dto.CreateSessionRequest;
import com.zhihu.fust.ai.chat.dto.SessionDetailResponse;
import com.zhihu.fust.ai.chat.dto.SessionResponse;
import com.zhihu.fust.ai.chat.dto.UpdateSessionTitleRequest;
import com.zhihu.fust.ai.chat.service.ChatService;
import com.zhihu.fust.ai.chat.service.ChatSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * 聊天控制器
 * 提供AI对话功能的Web接口，支持会话管理
 * 现在使用 Spring AI ChatMemory 自动管理聊天记忆
 * <p>
 * 流式响应优化：
 * - 使用缓冲机制减少HTTP传输次数
 * - 当缓冲区达到指定大小或超时时才发送数据
 * - 提高通信效率，减少网络开销
 */
@Controller
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatController.class);

    // 缓冲优化配置
    private static final int BUFFER_SIZE = 10; // 最大缓冲字符数：当累积到10个字符时立即发送
    private static final Duration BUFFER_TIMEOUT = Duration.ofMillis(100); // 最大等待时间：超过100ms后强制发送缓冲区内容

    private final ChatService chatService;
    private final ChatSessionService springAISessionService;

    public ChatController(ChatService chatService, ChatSessionService springAISessionService) {
        this.chatService = chatService;
        this.springAISessionService = springAISessionService;
    }

    // ==================== 会话管理接口 ====================

    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    @ResponseBody
    public ResponseEntity<SessionResponse> createSession(@RequestBody CreateSessionRequest request) {
        LOGGER.info("收到创建会话请求，标题: {}", request.getTitle());
        try {
            SessionResponse response = springAISessionService.createSession(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("创建会话失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/sessions")
    @ResponseBody
    public ResponseEntity<List<SessionResponse>> getSessions() {
        LOGGER.info("收到获取会话列表请求");
        try {
            List<SessionResponse> sessions = springAISessionService.getSessions();
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            LOGGER.error("获取会话列表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取会话详情
     */
    @GetMapping("/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<SessionDetailResponse> getSessionDetail(@PathVariable String sessionId) {
        LOGGER.info("收到获取会话详情请求，会话ID: {}", sessionId);
        try {
            SessionDetailResponse response = springAISessionService.getSessionDetail(sessionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("获取会话详情失败，会话ID: {}", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/sessions/{sessionId}/title")
    @ResponseBody
    public ResponseEntity<Void> updateSessionTitle(@PathVariable String sessionId, @RequestBody UpdateSessionTitleRequest request) {
        LOGGER.info("收到更新会话标题请求，会话ID: {}, 新标题: {}", sessionId, request.getTitle());
        try {
            springAISessionService.updateSessionTitle(sessionId, request.getTitle());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LOGGER.error("更新会话标题失败，会话ID: {}", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseBody
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        LOGGER.info("收到删除会话请求，会话ID: {}", sessionId);
        try {
            springAISessionService.deleteSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            LOGGER.error("删除会话失败，会话ID: {}", sessionId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== 聊天接口 ====================

    /**
     * 发送消息到AI（流式响应 - 带会话上下文）
     * 现在使用 Spring AI ChatMemory 自动管理聊天记忆
     * <p>
     * 缓冲优化机制：
     * - 使用 bufferTimeout() 操作符对AI响应进行智能缓冲
     * - 当缓冲区达到 BUFFER_SIZE 个字符或超过 BUFFER_TIMEOUT 时间时，合并发送
     * - 有效减少网络传输次数，提高通信效率
     * - 保证响应及时性，避免用户感知到延迟
     */
    @GetMapping(value = "/sessions/{sessionId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Flux<String> sendMessageStreamWithSession(@PathVariable String sessionId, @RequestParam String message) {
        LOGGER.info("收到会话流式聊天请求，会话ID: {}, 消息长度: {}", sessionId, message.length());
        try {
            // 验证会话是否存在
            if (!springAISessionService.sessionExists(sessionId)) {
                throw new RuntimeException("会话不存在: " + sessionId);
            }

            return chatService.chatStream(sessionId, message)
                    .map(content -> {
                        LOGGER.debug("收到AI响应片段: |{}|", content);
                        return content;
                    })
                    // 使用缓冲机制优化通信效率
                    .bufferTimeout(BUFFER_SIZE, BUFFER_TIMEOUT)
                    .filter(buffer -> !buffer.isEmpty())
                    .map(buffer -> {
                        // 将缓冲的内容合并为一个字符串
                        String combinedContent = String.join("", buffer);
                        LOGGER.debug("发送缓冲内容到前端，字符数: {}, 内容: |{}|",
                                combinedContent.length(), combinedContent);
                        return combinedContent;
                    })
                    .doOnComplete(() -> {
                        // 更新会话活动时间
                        springAISessionService.updateSessionActivity(sessionId);
                        LOGGER.info("会话流式响应完成，会话ID: {}", sessionId);
                    })
                    .doOnError(error -> {
                        LOGGER.error("会话流式响应失败，会话ID: {}", sessionId, error);
                    });
        } catch (Exception e) {
            LOGGER.error("会话流式聊天请求处理失败，会话ID: {}", sessionId, e);
            throw e;
        }
    }
}
