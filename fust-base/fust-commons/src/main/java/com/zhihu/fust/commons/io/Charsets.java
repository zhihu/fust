package com.zhihu.fust.commons.io;

import java.nio.charset.Charset;

public class Charsets {

    /**
     * Returns the given Charset or the default Charset if the given Charset is null.
     *
     * @param charset A charset or null.
     * @return the given Charset or the default Charset if the given Charset is null
     */
    public static Charset toCharset(final Charset charset) {
        return charset == null ? Charset.defaultCharset() : charset;
    }

    /**
     * Returns a Charset for the named charset. If the name is null, return the default Charset.
     *
     * @param charsetName The name of the requested charset, may be null.
     * @return a Charset for the named charset
     * @throws java.nio.charset.UnsupportedCharsetException If the named charset is unavailable
     */
    public static Charset toCharset(final String charsetName) {
        return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
    }
}
