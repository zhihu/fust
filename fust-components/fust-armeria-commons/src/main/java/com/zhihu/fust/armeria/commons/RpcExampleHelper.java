package com.zhihu.fust.armeria.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.linecorp.armeria.server.docs.DocServiceBuilder;

/**
 * load examples and add to armeria doc service
 * format like this:
 * <p>
 * if thrift interface method is
 * <p>
 * <code> string hi(1: string name); </code> <p>
 * </code> string order(1: OrderRequest req); </code>
 * <p>
 * json would be: <p>
 * {<p>
 * "hi": [{"name":"dan1"}, {"name":"dan2"}], <p>
 * "order": [{"req": {"sku_id":"11" }}, {"req": {"sku_id":"11" }}]<p>
 * }
 * <p>
 * for grpc, use request body directly,
 * <code> message OrderRequest{ 1: string sku_id}</code>
 * <code> OrderResponse order(OrderRequest req); </code>
 * json would be: <p>
 * {<p>
 * "order": [{"sku_id":"11" }, {"sku_id":"11" }]<p>
 * }
 */
public class RpcExampleHelper {
    private static final Logger log = LoggerFactory.getLogger(RpcExampleHelper.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    private final DocServiceBuilder docServiceBuilder;
    private final ExampleProvider exampleProvider;

    public RpcExampleHelper(DocServiceBuilder docServiceBuilder) {
        this(docServiceBuilder, new DefaultExampleProvider());
    }

    public RpcExampleHelper(DocServiceBuilder docServiceBuilder, ExampleProvider exampleProvider) {
        this.docServiceBuilder = docServiceBuilder;
        this.exampleProvider = exampleProvider;
    }

    /**
     * 通过文件获取 example 内容
     *
     * @param serviceName service name
     * @param fileName    example file name
     * @return for builder
     */
    public RpcExampleHelper addExample(String serviceName, String fileName) {
        return addExampleContent(serviceName, exampleProvider.getExample(fileName));
    }

    /**
     * 传入 exampleContent 内容
     *
     * @param serviceName    服务名称
     * @param exampleContent 请求 exampleContent 内容
     * @return
     */
    private RpcExampleHelper addExampleContent(String serviceName, String exampleContent) {
        Map<String, List<String>> examples = parseContent(serviceName, exampleContent);
        examples.forEach((methodName, requests) ->
                docServiceBuilder.exampleRequests(serviceName, methodName, requests));
        return this;
    }

    private Map<String, List<String>> parseContent(String serviceName, String content) {
        final Map<String, List<String>> examples = new HashMap<>();
        try {
            final JsonNode jsonNode = OBJECT_MAPPER.readTree(content);
            final Iterator<String> fieldNames = jsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String methodName = fieldNames.next();
                List<String> requests = new ArrayList<>();
                ArrayNode argsNode = (ArrayNode) jsonNode.get(methodName);
                argsNode.forEach(x -> {
                    requests.add(x.toPrettyString());
                });
                List<String> list = examples.computeIfAbsent(methodName,
                        key -> new ArrayList<>());
                list.addAll(requests);
            }
        } catch (Exception e) {
            log.error("parse error, for serviceName|" + serviceName, e);
        }
        return examples;
    }
}
