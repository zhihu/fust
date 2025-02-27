package com.zhihu.fust.armeria.commons;

import com.linecorp.armeria.common.RequestHeaders;
import com.zhihu.fust.telemetry.api.Telemetry;
import io.netty.util.AsciiString;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class ArmeriaTelemetry {
    private static final Telemetry TELEMETRY = Telemetry.create("serverLogDecorator");
    private static final Logger log = LoggerFactory.getLogger(ArmeriaServerRequestLogDecorator.class);

    private static final TextMapGetter<RequestHeaders> GETTER = new TextMapGetter<RequestHeaders>() {
        @Override
        public Iterable<String> keys(RequestHeaders headers) {
            return headers.names()
                    .stream()
                    .map(AsciiString::toString)
                    .collect(Collectors.toList());
        }

        @Override
        public String get(RequestHeaders carrier, @Nonnull String key) {
            if (carrier != null) {
                return carrier.get(key);
            }
            return null;
        }
    };

    public static Context extract(Context context, RequestHeaders headers) {
        return TELEMETRY.getTextMapPropagator().extract(context, headers, GETTER);
    }


}
