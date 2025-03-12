package com.zhihu.fust.telemetry.api;

public interface ServiceMeterCollector {
    void collect(ServiceMeter meter);
}
