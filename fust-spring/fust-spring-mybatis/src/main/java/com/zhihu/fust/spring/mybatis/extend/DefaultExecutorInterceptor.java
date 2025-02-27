package com.zhihu.fust.spring.mybatis.extend;

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.zhihu.fust.spring.mybatis.TableMeta;
import com.zhihu.fust.spring.mybatis.operations.SqlOperation;
import com.zhihu.fust.spring.mybatis.util.TableMetaUtil;

@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = { MappedStatement.class, Object.class }),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {
                        MappedStatement.class,
                        Object.class,
                        RowBounds.class,
                        ResultHandler.class,
                        CacheKey.class,
                        BoundSql.class
                }),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {
                        MappedStatement.class,
                        Object.class,
                        RowBounds.class,
                        ResultHandler.class
                }),
        @Signature(
                type = Executor.class,
                method = "queryCursor",
                args = {
                        MappedStatement.class,
                        Object.class,
                        RowBounds.class
                }),
})

public class DefaultExecutorInterceptor implements Interceptor {

    private static final DefaultExecutorInterceptorContext EMPTY = name -> {
    };

    private final DefaultExecutorInterceptorContext context;

    public DefaultExecutorInterceptor(DefaultExecutorInterceptorContext context) {
        if (context == null) {
            context = EMPTY;
        }
        this.context = context;
    }

    private void validatePatch(TableMeta tableMeta, MappedStatement mappedStatement) {
        String msId = mappedStatement.getId();
        String method = msId.substring(msId.lastIndexOf(".") + 1);
        boolean isPatchMethod = method.equalsIgnoreCase(SqlOperation.BATCH_PATCH.name())
                                || method.equalsIgnoreCase(SqlOperation.PATCH.name());
        if (isPatchMethod && tableMeta != null && !tableMeta.isSupportPatch()) {
            throw new IllegalArgumentException("patch method not support base type.table name:"
                                               + tableMeta.getTableName());
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object result = null;
        if (target instanceof Executor) {
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement) args[0];
            TableMeta tableMeta = TableMetaUtil.getMetaByMapperName(ms.getResource());

            validatePatch(tableMeta, ms);

            if (tableMeta != null) {
                String schema = tableMeta.getSchemaName();
                context.setDatabaseName(schema);
            }

            try {
                result = invocation.proceed();
            } finally {
                // clear db
                context.setDatabaseName("");
            }
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
