package com.zhihu.fust.commons.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * exception utils
 *
 * @author yzz
 * @since 2021-04-28
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    private static final int LEN = 2;

    public static Throwable getRootCause(final Throwable throwable) {
        final List<Throwable> list = getThrowableList(throwable);
        return list.size() < LEN ? throwable : list.get(list.size() - 1);
    }

    public static List<Throwable> getThrowableList(Throwable throwable) {
        final List<Throwable> list = new ArrayList<>();
        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }
}
