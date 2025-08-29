package com.zhihu.fust.ai.chat.tool;

import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

/**
 * 提供网络搜索功能的工具。
 * 允许大语言模型执行网络搜索以获取实时信息或补充知识。
 */
public interface WebSearchTool {

    /**
     * 表示单个网络搜索结果的记录。
     */
    interface WebSearchResult {
        /**
         * @return 搜索结果的标题。
         */
        String title();

        /**
         * @return 搜索结果内容的摘要或片段。
         */
        String content();

        /**
         * @return 搜索结果的原始网页链接。
         */
        String link();
    }

    /**
     * 表示网络搜索操作的响应。
     */
    interface WebSearchResponse {
        /**
         * @return 包含多个 WebSearchResult 对象的列表。
         */
        List<WebSearchResult> results();
    }

    /**
     * 网络搜索请求的输入参数。
     *
     * @param queryMessage 用户或模型希望搜索的查询字符串。
     * @param count        希望返回的搜索结果数量，通常在 1 到 50 之间。
     */
    record WebSearchRequest(String queryMessage, int count) {
    }

    /**
     * 执行网络搜索并返回相关结果。
     * 此工具用于获取最新的、实时的或模型训练数据中可能不包含的信息。
     *
     * @param request 包含搜索查询和结果数量的请求对象。
     * @return 包含搜索结果列表的响应对象。
     */
    WebSearchResponse search(WebSearchRequest request);
}