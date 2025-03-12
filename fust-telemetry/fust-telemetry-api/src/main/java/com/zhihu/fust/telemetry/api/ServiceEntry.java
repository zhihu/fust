package com.zhihu.fust.telemetry.api;

import java.util.HashMap;
import java.util.Map;

import io.opentelemetry.context.ContextKey;

public class ServiceEntry {
    public static final ContextKey<ServiceEntry> SERVICE_ENTRY_KEY = ContextKey.named("service-entry-key");

    public static final ServiceEntry UNKNOWN_ENTRY = new ServiceEntry("unknown-entry");
    /**
     * service entry method
     */
    private final String entry;
    private Map<String, String> extra;

    public String getEntry() {
        return entry;
    }

    public ServiceEntry(String entry) {
        this.entry = entry;
        this.extra = new HashMap<>();
    }

    public void addExtraValue(String key, String value) {
        this.extra.put(key, value);
    }

    public String getExtraValue(String key) {
        return this.extra.get(key);
    }

    public static ServiceEntry create(String entry) {
        return new ServiceEntry(entry);
    }
}
