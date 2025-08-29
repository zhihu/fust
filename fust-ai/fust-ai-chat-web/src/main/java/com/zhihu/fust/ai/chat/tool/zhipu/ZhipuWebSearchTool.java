package com.zhihu.fust.ai.chat.tool.zhipu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihu.fust.ai.chat.tool.WebSearchTool;
import com.zhihu.fust.commons.lang.PropertyUtils;
import org.springframework.ai.tool.annotation.Tool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

public class ZhipuWebSearchTool implements WebSearchTool {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_KEY_PROP = "ZHIPU_API_KEY";
    private String apiKey;

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/web_search";
    private final String searchEngine;

    public ZhipuWebSearchTool() {
        this("search_std");
    }

    public ZhipuWebSearchTool(String searchEngine) {
        this.searchEngine = searchEngine;
        this.apiKey = PropertyUtils.getProperty(API_KEY_PROP);
    }


    private ZhipuWebSearchResponse callWebSearch(ZhipuWebSearchRequest requestBody) throws IOException, InterruptedException {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Exclude null fields from JSON
        HttpClient client = HttpClient.newHttpClient();
        String requestJson = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), ZhipuWebSearchResponse.class);
    }

    @Override
    @Tool(name = "webSearch", description = "执行网络搜索以获取实时信息或补充知识。")
    public WebSearchResponse search(WebSearchRequest request) {
        var zhipuRequest = new ZhipuWebSearchRequest(request.queryMessage(), searchEngine, request.count(), null);
        try {
            return callWebSearch(zhipuRequest);
        } catch (Exception e) {
            throw new RuntimeException("invoke zhipu search tool error", e);
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ZhipuWebSearchRequest(
            @JsonProperty("search_query") String searchQuery,
            @JsonProperty("search_engine") String searchEngine,
            @JsonProperty("count") Integer count,
            @JsonProperty("content_size") Integer contentSize
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ZhipuWebSearchResponse(
            String id,
            Long created,
            @JsonProperty("request_id") String requestId,
            @JsonProperty("search_intent") List<SearchIntent> searchIntent,
            @JsonProperty("search_result") List<ZhipuSearchResult> searchResult
    ) implements WebSearchResponse {

        @Override
        public List<WebSearchResult> results() {
            if (searchResult == null) {
                return List.of();
            }
            return searchResult.stream()
                    .map(result -> (WebSearchResult) result)
                    .collect(Collectors.toList());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SearchIntent(
            String query,
            String intent,
            String keywords
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ZhipuSearchResult(
            String title,
            String content,
            String link,
            String media,
            String icon,
            String refer,
            @JsonProperty("publish_date") String publishDate
    ) implements WebSearchResult {
    }

}
