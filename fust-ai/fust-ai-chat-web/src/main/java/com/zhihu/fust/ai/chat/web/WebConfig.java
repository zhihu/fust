package com.zhihu.fust.ai.chat.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.ResourceResolver;
import org.springframework.web.reactive.resource.ResourceResolverChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class WebConfig implements WebFluxConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源处理
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    /**
     * SPA资源解析器
     * 对于前端路由，返回index.html
     */
    public static class SpaResourceResolver implements ResourceResolver {

        private final Resource index = new ClassPathResource("static/index.html");
        private final List<String> handledExtensions = List.of("js", "css", "ico", "png",
                "jpg", "jpeg", "gif", "svg", "woff", "woff2", "ttf", "eot", "map", "json");
        private final List<String> ignoredPaths = List.of("api");

        @Override
        public Mono<Resource> resolveResource(ServerWebExchange exchange, String requestPath,
                                              List<? extends Resource> locations, ResourceResolverChain chain) {
            return chain.resolveResource(exchange, requestPath, locations)
                    .switchIfEmpty(Mono.defer(() -> resolveIndexHtml(exchange)));
        }

        private Mono<Resource> resolveIndexHtml(ServerWebExchange exchange) {
            String path = exchange.getRequest().getPath().value();

            // 如果是API路径，不处理
            if (isIgnoredPath(path)) {
                return Mono.empty();
            }

            // 如果是静态资源文件，不处理
            if (isStaticResource(path)) {
                return Mono.empty();
            }

            // 其他路径返回index.html，让前端路由处理
            return Mono.just(index);
        }

        @Override
        public Mono<String> resolveUrlPath(String resourcePath, List<? extends Resource> locations,
                                           ResourceResolverChain chain) {
            return chain.resolveUrlPath(resourcePath, locations);
        }

        private boolean isIgnoredPath(String path) {
            return ignoredPaths.stream().anyMatch(ignored ->
                    path.startsWith("/" + ignored + "/") || path.equals("/" + ignored));
        }

        private boolean isStaticResource(String path) {
            if (path.contains(".")) {
                String extension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                return handledExtensions.contains(extension);
            }
            return false;
        }
    }
}
