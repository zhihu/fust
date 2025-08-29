package com.zhihu.fust.ai.chat.service;

import reactor.core.publisher.Flux;

/**
 * chat service
 */
public interface ChatService {
    public Flux<String> chatStream(String conversationId, String message);

    void deleteConversation(String sessionId);
}
