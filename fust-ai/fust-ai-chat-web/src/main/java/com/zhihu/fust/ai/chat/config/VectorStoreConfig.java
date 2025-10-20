package com.zhihu.fust.ai.chat.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 * <p>
 * 这是一个示例配置，展示如何配置不同类型的向量存储。
 * 在生产环境中，建议使用更高性能的向量存储，如：
 * - PGVector (PostgreSQL)
 * - Redis
 * - Chroma
 * - Pinecone
 * - Weaviate
 * 等
 */
@Configuration
@ConditionalOnProperty(name = "ai.chat.web.rag.enabled", havingValue = "true")
public class VectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreConfig.class);

    /**
     * 简单向量存储配置（仅用于开发和测试）
     * <p>
     * 注意：SimpleVectorStore 将数据存储在内存中，不适合生产环境。
     * 在生产环境中，请使用持久化的向量存储。
     */
    @Bean
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        log.info("创建 SimpleVectorStore（内存向量存储）- 仅适用于开发和测试环境");
        log.warn("警告：SimpleVectorStore 不会持久化数据，重启后数据将丢失");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
