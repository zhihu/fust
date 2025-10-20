package com.zhihu.fust.ai.chat.service;

import com.zhihu.fust.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 基于 Spring AI ChatMemory 的聊天服务
 * 提供带记忆的聊天功能、流式响应和 RAG (Retrieval Augmented Generation) 支持
 */
public class BasicChatService implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(BasicChatService.class);
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个智能助手，能够分析问题，并根据用户提供的工具来解决问题";

    private final ChatModel chatModel;
    private final ChatMemoryRepository chatMemoryRepository;
    private final ChatHistoryService chatHistoryService;
    private SyncMcpToolCallbackProvider mcpToolCallbackProvider;
    @Value("${ai.chat.web.system.prompt:}")
    private String systemPrompt;

    @Value("${ai.chat.web.memory.max.message:10}")
    private int maxMessageCount;

    // RAG 相关配置
    @Value("${ai.chat.web.rag.enabled:false}")
    private boolean ragEnabled;

    @Value("${ai.chat.web.rag.similarity.threshold:0.7}")
    private double similarityThreshold;

    @Value("${ai.chat.web.rag.top.k:5}")
    private int topK;

    private VectorStore vectorStore;

    private ChatClient chatClient;
    private List<Object> tools;

    public BasicChatService(ChatModel chatModel, ChatMemoryRepository chatMemoryRepository, ChatHistoryService chatHistoryService, List<Object> tools) {
        this.chatModel = chatModel;
        this.chatMemoryRepository = chatMemoryRepository;
        this.chatHistoryService = chatHistoryService;
        this.tools = tools;
    }

    public void setVectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void setMcpToolCallbackProvider(SyncMcpToolCallbackProvider toolCallbackProvider) {
        this.mcpToolCallbackProvider = toolCallbackProvider;
    }

    /**
     * 创建带记忆和 RAG 支持的 ChatClient
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

        if (mcpToolCallbackProvider != null) {
            builder.defaultToolCallbacks(mcpToolCallbackProvider);
        }

        // 添加记忆 Advisor
        var memoryAdvisor = MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        // 如果启用了 RAG 且有向量存储，添加 QuestionAnswerAdvisor
        if (ragEnabled && vectorStore != null) {
            log.info("RAG 功能已启用，相似度阈值: {}, TopK: {}", similarityThreshold, topK);
            var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder()
                            .similarityThreshold(similarityThreshold)
                            .topK(topK)
                            .build())
                    .build();
            return builder.defaultAdvisors(memoryAdvisor, qaAdvisor).build();
        } else {
            if (ragEnabled) {
                log.warn("RAG 功能已启用但未找到 VectorStore Bean，将使用普通聊天模式");
            }
            return builder.defaultAdvisors(memoryAdvisor).build();
        }
    }

    private ChatClient getChatClient() {
        if (chatClient == null) {
            chatClient = createChatClientWithMemory();
        }
        return chatClient;
    }

    /**
     * 发送消息并获取流式响应（带会话记忆）
     *
     * @param conversationId 会话ID
     * @param message        用户消息
     * @return AI的流式回复
     */
    public Flux<String> chatStream(String conversationId, String message) {
        return chatStream(conversationId, message, null);
    }

    /**
     * 发送消息并获取流式响应（带会话记忆和 RAG 过滤）
     *
     * @param conversationId   会话ID
     * @param message          用户消息
     * @param filterExpression RAG 过滤表达式（可选）
     * @return AI的流式回复
     */
    public Flux<String> chatStream(String conversationId, String message, String filterExpression) {
        log.info("开始会话流式AI聊天，会话ID: {}, 消息长度: {}, RAG过滤: {}",
                conversationId, message.length(), filterExpression != null ? filterExpression : "无");
        log.debug("用户消息: {}", message);

        try {
            // save user message
            chatHistoryService.saveMessage(MessageType.USER, conversationId, message);
            // 用一个 StringBuilder 收集所有内容
            StringBuilder aiReply = new StringBuilder();

            var promptBuilder = getChatClient().prompt()
                    .user(message)
                    .advisors(a -> {
                        a.param(ChatMemory.CONVERSATION_ID, conversationId);
                        // 如果启用了 RAG 且提供了过滤表达式，设置动态过滤
                        if (ragEnabled && vectorStore != null && StringUtils.isNotEmpty(filterExpression)) {
                            a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, filterExpression);
                            log.debug("应用 RAG 过滤表达式: {}", filterExpression);
                        }
                    });

            return promptBuilder
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

    /**
     * 检查 RAG 功能是否可用
     *
     * @return true 如果 RAG 功能已启用且向量存储可用
     */
    public boolean isRagAvailable() {
        return ragEnabled && vectorStore != null;
    }

    /**
     * 获取 RAG 配置信息
     *
     * @return RAG 配置信息的字符串表示
     */
    public String getRagConfigInfo() {
        if (!ragEnabled) {
            return "RAG 功能未启用";
        }
        if (vectorStore == null) {
            return "RAG 功能已启用，但未找到 VectorStore";
        }
        return String.format("RAG 功能已启用 - 相似度阈值: %.2f, TopK: %d, VectorStore: %s",
                similarityThreshold, topK, vectorStore.getClass().getSimpleName());
    }

} 