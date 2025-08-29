package com.zhihu.fust.ai.chat.web;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

/**
 * 首页控制器
 * 处理SPA路由和静态资源
 */
@Controller
public class IndexController {

    private final Resource indexResource = new ClassPathResource("static/index.html");

    /**
     * 根路径重定向到chat页面
     */
    @GetMapping("/")
    public String redirectToChat() {
        return "redirect:/chat";
    }
    
    /**
     * 前端路由都返回index.html
     * 让Nuxt处理客户端路由
     */
    @GetMapping(value = {"/chat", "/chat/**", "/settings", "/settings/**"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<Resource> index() {
        return Mono.just(indexResource);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    @ResponseBody
    public Mono<String> health() {
        return Mono.just("{\"status\":\"ok\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}");
    }
} 