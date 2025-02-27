package com.zhihu.fust.telemetry.lettuce;

import com.zhihu.fust.telemetry.api.Telemetry;

public final class LettuceTelemetry {
    private static final Telemetry TELEMETRY = Telemetry.create("lettuce");

    private LettuceTelemetry() {
    }

    public static Telemetry getTelemetry() {
        return TELEMETRY;
    }
}
