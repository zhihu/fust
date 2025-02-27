package com.zhihu.fust.spring.mybatis.operations;

import org.apache.ibatis.builder.MapperBuilderAssistant;

public interface Operation {
    void bind(MapperBuilderAssistant builderAssistant, Class<?> mapperClass);
}
