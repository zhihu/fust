package com.zhihu.fust.armeria.commons;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhihu.fust.commons.io.IOUtils;

/***
 * default example provider
 * load from class loader resource if exists
 */
public class DefaultExampleProvider implements ExampleProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultExampleProvider.class);

    private final String path;

    public DefaultExampleProvider() {
        this("");
    }

    public DefaultExampleProvider(String path) {
        this.path = path;
    }

    @Override
    @Nullable
    public String getExample(String fileName) {
        if (!path.isEmpty()) {
            fileName = path + '/' + fileName;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        if (classLoader == null) {
            return null;
        }
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (stream != null) {
                return IOUtils.toString(stream, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("read file error,file|" + fileName, e);
        }
        return null;
    }
}
