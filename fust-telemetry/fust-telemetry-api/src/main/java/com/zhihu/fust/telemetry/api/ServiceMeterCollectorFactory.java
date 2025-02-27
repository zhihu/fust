package com.zhihu.fust.telemetry.api;

public interface ServiceMeterCollectorFactory {
    ServiceMeterCollector create(String name);
}
