package com.hogwai.batch.core.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe key-value store shared between steps within a job execution.
 * Provides typed accessors for common value types.
 */
public class ExecutionContext {
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    /**
     * Stores a value under the given key.
     *
     * @param key   the entry key
     * @param value the entry value
     */
    public void put(String key, Object value) { map.put(key, value); }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the entry key
     * @return the value, or {@code null} if not present
     */
    public Object get(String key) { return map.get(key); }

    /**
     * Stores a string value under the given key.
     *
     * @param key   the entry key
     * @param value the string value
     */
    public void putString(String key, String value) { map.put(key, value); }

    /**
     * Retrieves the string value associated with the given key.
     *
     * @param key the entry key
     * @return the string value, or {@code null} if not present
     */
    public String getString(String key) { return (String) map.get(key); }

    /**
     * Stores a long value under the given key.
     *
     * @param key   the entry key
     * @param value the long value
     */
    public void putLong(String key, long value) { map.put(key, value); }

    /**
     * Retrieves the long value associated with the given key.
     *
     * @param key the entry key
     * @return the long value, or {@code null} if not present
     */
    public Long getLong(String key) { return (Long) map.get(key); }

    /**
     * Checks whether an entry exists for the given key.
     *
     * @param key the entry key
     * @return {@code true} if the key is present
     */
    public boolean containsKey(String key) { return map.containsKey(key); }

    /**
     * Returns {@code true} if this context contains no entries.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() { return map.isEmpty(); }

    /**
     * Copies all entries from another {@code ExecutionContext} into this one.
     *
     * @param other the context to copy from
     */
    public void putAll(ExecutionContext other) { map.putAll(other.map); }
}
