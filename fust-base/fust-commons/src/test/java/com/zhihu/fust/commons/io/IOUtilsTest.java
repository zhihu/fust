package com.zhihu.fust.commons.io;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IOUtilsTest {
    @Test
    void testIO() throws IOException {
        String text = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.write(text, outputStream, StandardCharsets.UTF_8);

        assertEquals(text, outputStream.toString(StandardCharsets.UTF_8.name()));

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        List<String> strings = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);

        assertEquals(10, strings.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(String.valueOf(i + 1), strings.get(i));
        }

        inputStream.reset();
        strings = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        for (int i = 0; i < 10; i++) {
            assertEquals(String.valueOf(i + 1), strings.get(i));
        }

        inputStream.reset();
        String value = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        assertEquals(text, value);

        BufferedReader bufferedReader = IOUtils.toBufferedReader(new StringReader(text));
        String line = bufferedReader.readLine();
        assertEquals("1", line);
        IOUtils.closeQuietly(inputStream);

        StringReader sr = new StringReader(text);
        StringWriter sw = new StringWriter();
        IOUtils.copyLarge(sr, sw);
        assertEquals(text, sw.toString());
    }
}