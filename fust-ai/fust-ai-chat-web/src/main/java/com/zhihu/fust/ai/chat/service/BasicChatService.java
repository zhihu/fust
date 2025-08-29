package com.zhihu.fust.ai.chat.service;

import com.zhihu.fust.ai.chat.tool.zhipu.ZhipuWebSearchTool;
import com.zhihu.fust.commons.lang.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 基于 Spring AI ChatMemory 的聊天服务
 * 提供带记忆的聊天功能和流式响应
 */
public class BasicChatService implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(BasicChatService.class);
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个智能助手，能够分析问题，并根据用户提供的工具来解决问题";

    private final ChatModel chatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatHistoryService chatHistoryService;
    @Value("${ai.chat.web.system.prompt:}")
    private String systemPrompt;

    @Value("${ai.chat.web.memory.max.message:10}")
    private int maxMessageCount;
    private ChatClient chatClient;
    private List<Object> tools;

    public BasicChatService(ChatModel chatModel, ChatMemoryRepository chatMemoryRepository, ChatHistoryService chatHistoryService, List<Object> tools) {
        this.chatModel = chatModel;
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatHistoryService = chatHistoryService;
        this.tools = tools;
    }

    @PostConstruct
    private void init() {
        chatClient = createChatClientWithMemory();
    }

    /**
     * 创建带记忆的 ChatClient
     */
    private ChatClient createChatClientWithMemory() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessageCount)
                .build();
        var builder = ChatClient.builder(chatModel);

        if (StringUtils.isNotEmpty(systemPrompt)) {
            builder.defaultSystem(systemPrompt);
        } else {
            builder.defaultSystem(DEFAULT_SYSTEM_PROMPT);
        }
        if (!CollectionUtils.isEmpty(tools)) {
            builder.defaultTools(this.tools.toArray(new Object[0]));
        }
        var advisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
        return builder.defaultAdvisors(advisor).build();
    }

    /**
     * 发送消息并获取流式响应（带会话记忆）
     *
     * @param conversationId 会话ID
     * @param message        用户消息
     * @return AI的流式回复
     */
    public Flux<String> chatStream(String conversationId, String message) {
        log.info("开始会话流式AI聊天，会话ID: {}, 消息长度: {}", conversationId, message.length());
        log.debug("用户消息: {}", message);


        try {
            // 用一个 StringBuilder 收集所有内容
            StringBuilder aiReply = new StringBuilder();

            return chatClient.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .stream()
                    .content()
                    .doOnSubscribe(subscription -> {
                        log.info("AI流式响应开始订阅，会话ID: {}", conversationId);
                    })
                    .doOnNext(content -> {
                        log.debug("收到AI流式内容: [{}]", content);
                        aiReply.append(content);
                    })
                    .doOnError(error -> {
                        log.error("AI流式响应过程中出现错误，会话ID: {}, 用户消息: {}", conversationId, message, error);
                    })
                    .doOnComplete(() -> {
                        // 保存 AI 回复到 chatMemoryRepository
                        chatHistoryService.saveMessage(MessageType.ASSISTANT, conversationId, aiReply.toString());
                    })
                    .doOnCancel(() -> {
                        log.warn("AI流式响应被取消，会话ID: {}, 用户消息: {}", conversationId, message);
                    });
        } catch (Exception e) {
            log.error("创建会话AI流式聊天失败，会话ID: {}, 用户消息: {}", conversationId, message, e);
            throw e;
        }
    }

    /**
     * 删除会话的聊天记忆
     *
     * @param conversationId 会话ID
     */
    public void deleteConversation(String conversationId) {
        log.info("删除会话记忆，会话ID: {}", conversationId);
        try {
            chatMemoryRepository.deleteByConversationId(conversationId);
            chatHistoryService.deleteByConversationId(conversationId);
            log.info("成功删除会话记忆，会话ID: {}", conversationId);
        } catch (Exception e) {
            log.error("删除会话记忆失败，会话ID: {}", conversationId, e);
            throw new RuntimeException("删除会话记忆失败", e);
        }
    }


} 