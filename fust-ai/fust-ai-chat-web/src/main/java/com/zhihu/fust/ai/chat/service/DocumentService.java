package com.zhihu.fust.ai.chat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文档管理服务
 * 提供向量存储中文档的添加、删除、搜索等功能
 */
@Service
@ConditionalOnProperty(name = "ai.chat.web.rag.enabled", havingValue = "true")
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    @Autowired(required = false)
    private VectorStore vectorStore;

    /**
     * 添加文档到向量存储
     *
     * @param content  文档内容
     * @param metadata 文档元数据
     * @return 文档ID
     */
    public String addDocument(String content, Map<String, Object> metadata) {
        if (vectorStore == null) {
            throw new IllegalStateException("VectorStore 未配置，无法添加文档");
        }

        try {
            Document document = new Document(content, metadata);
            vectorStore.add(List.of(document));
            log.info("成功添加文档到向量存储，内容长度: {}, 元数据: {}", content.length(), metadata);
            return document.getId();
        } catch (Exception e) {
            log.error("添加文档到向量存储失败，内容长度: {}, 元数据: {}", content.length(), metadata, e);
            throw new RuntimeException("添加文档失败", e);
        }
    }

    /**
     * 批量添加文档到向量存储
     *
     * @param documents 文档列表
     */
    public void addDocuments(List<Document> documents) {
        if (vectorStore == null) {
            throw new IllegalStateException("VectorStore 未配置，无法添加文档");
        }

        try {
            vectorStore.add(documents);
            log.info("成功批量添加 {} 个文档到向量存储", documents.size());
        } catch (Exception e) {
            log.error("批量添加文档到向量存储失败，文档数量: {}", documents.size(), e);
            throw new RuntimeException("批量添加文档失败", e);
        }
    }

    /**
     * 搜索相似文档
     *
     * @param query 查询文本
     * @param topK  返回的文档数量
     * @return 相似文档列表
     */
    public List<Document> searchSimilarDocuments(String query, int topK) {
        return searchSimilarDocuments(query, topK, 0.0, null);
    }

    /**
     * 搜索相似文档（带过滤）
     *
     * @param query             查询文本
     * @param topK              返回的文档数量
     * @param similarityThreshold 相似度阈值
     * @param filterExpression  过滤表达式
     * @return 相似文档列表
     */
    public List<Document> searchSimilarDocuments(String query, int topK, double similarityThreshold, String filterExpression) {
        if (vectorStore == null) {
            throw new IllegalStateException("VectorStore 未配置，无法搜索文档");
        }

        try {
            SearchRequest.Builder builder = SearchRequest.builder()
                    .query(query)
                    .topK(topK);

            if (similarityThreshold > 0) {
                builder.similarityThreshold(similarityThreshold);
            }

            if (filterExpression != null && !filterExpression.trim().isEmpty()) {
                builder.filterExpression(filterExpression);
            }

            SearchRequest searchRequest = builder.build();
            List<Document> results = vectorStore.similaritySearch(searchRequest);
            
            log.debug("搜索相似文档，查询: {}, TopK: {}, 相似度阈值: {}, 过滤: {}, 结果数量: {}", 
                    query, topK, similarityThreshold, filterExpression, results.size());
            
            return results;
        } catch (Exception e) {
            log.error("搜索相似文档失败，查询: {}, TopK: {}, 相似度阈值: {}, 过滤: {}", 
                    query, topK, similarityThreshold, filterExpression, e);
            throw new RuntimeException("搜索文档失败", e);
        }
    }

    /**
     * 删除文档
     *
     * @param documentIds 文档ID列表
     */
    public void deleteDocuments(List<String> documentIds) {
        if (vectorStore == null) {
            throw new IllegalStateException("VectorStore 未配置，无法删除文档");
        }

        try {
            vectorStore.delete(documentIds);
            log.info("成功删除 {} 个文档", documentIds.size());
        } catch (Exception e) {
            log.error("删除文档失败，文档ID: {}", documentIds, e);
            throw new RuntimeException("删除文档失败", e);
        }
    }

    /**
     * 检查向量存储是否可用
     *
     * @return true 如果向量存储可用
     */
    public boolean isVectorStoreAvailable() {
        return vectorStore != null;
    }

    /**
     * 获取向量存储信息
     *
     * @return 向量存储信息
     */
    public String getVectorStoreInfo() {
        if (vectorStore == null) {
            return "VectorStore 未配置";
        }
        return String.format("VectorStore 类型: %s", vectorStore.getClass().getSimpleName());
    }
}
