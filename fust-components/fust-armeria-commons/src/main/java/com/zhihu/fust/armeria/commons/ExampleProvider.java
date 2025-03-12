package com.zhihu.fust.armeria.commons;

import javax.annotation.Nullable;

@FunctionalInterface
public interface ExampleProvider {

    /**
     * 通过 fileName 获取到 example 内容
     *
     * @param fileName example file name
     * @return example json 内容
     */
    @Nullable
    String getExample(String fileName);
}