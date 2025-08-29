package com.zhihu.fust.ai.chat.memory;

import com.zhihu.fust.ai.chat.service.ChatHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Component
public class SpringChatMemoryRepository implements ChatMemoryRepository {

    private static final Logger log = LoggerFactory.getLogger(SpringChatMemoryRepository.class);

    private final ChatHistoryService chatHistoryService;
    private final InMemoryChatMemoryRepository inMemoryChatMemoryRepository;
    @Value("${ai.chat.web.memory.max.message:10}")
    private int maxMessageCount;

    public SpringChatMemoryRepository(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
        this.inMemoryChatMemoryRepository = new InMemoryChatMemoryRepository();
    }

    @NotNull
    @Override
    public List<String> findConversationIds() {
        throw new UnsupportedOperationException("not support");
    }

    @NotNull
    @Override
    public List<Message> findByConversationId(@NotNull String conversationId) {
        log.debug("查找会话消息，会话ID: {}", conversationId);
        try {
            var messages = inMemoryChatMemoryRepository.findByConversationId(conversationId);
            if (messages.isEmpty()) {
                var historyMessages = chatHistoryService.getConversationForChatMemory(conversationId, maxMessageCount);
                if (historyMessages.size() == 1) {
                    // 这里特殊处理下，如果 historyMessages 消息数量为 1，理解返回空消息，避免重复添加
                    // 这个问题的原因是，首次请求时，我们会立即保存用户的消息，导致进入到这里时，已经存在了一个消息。
                    // 实际上这个消息再后面会 MessageWindowChatMemory 处理中，会再次加入到 memory 中
                    return messages;
                }
            }
            return messages;
        } catch (Exception e) {
            log.error("查找会话消息失败，会话ID: {}", conversationId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public void saveAll(@NotNull String conversationId, @NotNull List<Message> messages) {
        inMemoryChatMemoryRepository.saveAll(conversationId, messages);
    }

    @Override
    public void deleteByConversationId(@NotNull String conversationId) {
        try {
            inMemoryChatMemoryRepository.deleteByConversationId(conversationId);
        } catch (Exception e) {
            throw new RuntimeException("删除会话消息失败", e);
        }
    }
}
