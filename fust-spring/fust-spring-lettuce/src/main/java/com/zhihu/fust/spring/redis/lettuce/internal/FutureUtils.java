package com.zhihu.fust.spring.redis.lettuce.internal;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.springframework.data.redis.connection.lettuce.LettuceExceptionConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * @author yzz
 * @date 2020-04-20
 */
class FutureUtils {
    @Nullable
    static <T> T join(CompletionStage<T> future) throws RuntimeException, CompletionException {

        Assert.notNull(future, "CompletableFuture must not be null!");

        try {
            return future.toCompletableFuture().join();
        } catch (Exception e) {

            Throwable exceptionToUse = e;

            if (e instanceof CompletionException) {
                exceptionToUse = new LettuceExceptionConverter().convert((Exception) e.getCause());
                if (exceptionToUse == null) {
                    exceptionToUse = e.getCause();
                }
            }

            if (exceptionToUse instanceof RuntimeException) {
                throw (RuntimeException) exceptionToUse;
            }

            throw new CompletionException(exceptionToUse);
        }
    }
}
