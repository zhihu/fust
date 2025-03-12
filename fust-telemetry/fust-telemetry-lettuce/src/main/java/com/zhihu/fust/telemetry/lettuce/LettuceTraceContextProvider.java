package com.zhihu.fust.telemetry.lettuce;

import io.lettuce.core.tracing.TraceContext;
import io.lettuce.core.tracing.TraceContextProvider;
import io.opentelemetry.context.Context;

/**
 * use context.current as parent
 *
 * @author yanzhuzhu
 * @date 2020-01-15
 */
public class LettuceTraceContextProvider implements TraceContextProvider {

    @Override
    public TraceContext getTraceContext() {
        return new LettuceTraceContext(Context.current());
    }

}
