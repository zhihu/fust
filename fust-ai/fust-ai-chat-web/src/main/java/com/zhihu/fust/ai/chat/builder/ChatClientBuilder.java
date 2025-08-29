package com.zhihu.fust.ai.chat.builder;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatClientBuilder {

    private ChatClient.Builder builder;
    private int maxMessages;
    private ChatMemoryRepository chatMemoryRepository;
    private final List<Advisor> advisors;

    public ChatClientBuilder(ChatModel chatModel) {
        this.builder = ChatClient.builder(chatModel);
        this.advisors = new ArrayList<>();
    }

    public static ChatClientBuilder builder(ChatModel chatModel) {
        return new ChatClientBuilder(chatModel);
    }

    public ChatClientBuilder maxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
        return this;
    }

    public ChatClientBuilder defaultSystem(String text) {
        builder.defaultSystem(text);
        return this;
    }

    public ChatClientBuilder chatMemoryRepository(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
        return this;
    }

    private MessageChatMemoryAdvisor messageChatMemoryAdvisor() {
        if (chatMemoryRepository == null) {
            chatMemoryRepository = new InMemoryChatMemoryRepository();
        }
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(maxMessages)
                .build();
        return MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
    }

    public ChatClientBuilder defaultAdvisors(List<Advisor> advisors) {
        this.advisors.addAll(advisors);
        return this;
    }

    public ChatClient build() {
        return builder.defaultAdvisors(advisors)
                .build();
    }


}
