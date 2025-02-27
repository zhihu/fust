package com.zhihu.fust.telemetry.api;

import com.zhihu.fust.commons.exception.ExceptionUtils;
import com.zhihu.fust.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * service meter
 * service(client) -> service(server)
 */
public class ServiceMeter {
    private final ServiceMeterKind kind;
    private String method;
    private final Long startTime;
    private String targetService;
    private String targetMethod;
    private String error;
    private final List<String> tags;
    private final ServiceMeterCollector collector;

    public ServiceMeter(ServiceMeterKind kind, ServiceMeterCollector collector) {
        this.collector = collector;
        this.kind = kind;
        this.tags = new ArrayList<>();
        startTime = System.currentTimeMillis();
    }

    public ServiceMeterKind getKind() {
        return kind;
    }

    public String getMethod() {
        return method;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getTargetService() {
        return targetService;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return StringUtils.isNotEmpty(error);
    }

    public List<String> getTags() {
        return tags;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setError(Throwable throwable) {
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        error = rootCause.getClass().getSimpleName();
    }

    public void end() {
        collector.collect(this);
    }
}
