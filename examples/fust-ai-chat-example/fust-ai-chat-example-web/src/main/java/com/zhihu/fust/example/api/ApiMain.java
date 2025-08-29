package com.zhihu.fust.example.api;


import com.zhihu.fust.ai.chat.AiChatWebConfiguration;
import com.zhihu.fust.ai.chat.service.BasicChatService;
import com.zhihu.fust.ai.chat.service.ChatHistoryService;
import com.zhihu.fust.ai.chat.service.ChatService;
import com.zhihu.fust.ai.chat.tool.zhipu.ZhipuWebSearchTool;
import com.zhihu.fust.commons.lang.StringUtils;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@Import(AiChatWebConfiguration.class)
public class ApiMain {

    public List<Object> tools() {
        var tools = new ArrayList<>();
        // 智谱搜索工具 https://bigmodel.cn/dev/api/search-tool/web-search
        if (StringUtils.isNotEmpty(System.getenv("ZHIPU_API_KEY"))) {
            tools.add(new ZhipuWebSearchTool());
        }
        return tools;
    }

    @Bean
    public ChatService chatService(ChatModel chatModel, ChatMemoryRepository chatMemoryRepository, ChatHistoryService chatHistoryService) {
        return new BasicChatService(chatModel, chatMemoryRepository, chatHistoryService, tools());
    }

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApiMain.class);
        application.run(args);
    }
}
