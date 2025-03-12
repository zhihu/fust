package com.zhihu.fust.config.extension;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author wanghan
 * @since 2021/10/12 11:05 上午
 */
public class GrayConfigDeserializer extends JsonDeserializer<GrayConfig> {
    @Override
    public GrayConfig deserialize(JsonParser p, DeserializationContext context) throws IOException, JsonProcessingException {
        ObjectCodec codec = p.getCodec();
        Map<String, GrayConfig.GrayItem> grayItemMap = codec.readValue(p, new TypeReference<Map<String, GrayConfig.GrayItem>>() {
        });
        GrayConfig grayConfig = new GrayConfig();
        grayConfig.setGrayItems(grayItemMap);
        return grayConfig;
    }
}
