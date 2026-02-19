package com.hogwai.batch.core.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutionContext {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public void put(String key, Object value) { map.put(key, value); }
    public Object get(String key) { return map.get(key); }

    public void putString(String key, String value) { map.put(key, value); }
    public String getString(String key) { return (String) map.get(key); }

    public void putLong(String key, long value) { map.put(key, value); }
    public Long getLong(String key) { return (Long) map.get(key); }

    public boolean containsKey(String key) { return map.containsKey(key); }
    public boolean isEmpty() { return map.isEmpty(); }

    public void putAll(ExecutionContext other) { map.putAll(other.map); }
}
