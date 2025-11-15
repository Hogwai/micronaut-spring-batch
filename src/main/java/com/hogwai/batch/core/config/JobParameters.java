package com.hogwai.batch.core.config;

import java.util.HashMap;
import java.util.Map;

public final class JobParameters {
    private final Map<String, Object> parameters;

    private JobParameters(Map<String, Object> parameters) {
        this.parameters = Map.copyOf(parameters);
    }

    public static JobParametersBuilder builder() {
        return new JobParametersBuilder();
    }

    public Object get(String key) { return parameters.get(key); }
    public String getString(String key) { return (String) parameters.get(key); }
    public Long getLong(String key) { return (Long) parameters.get(key); }

    public static class JobParametersBuilder {
        private final Map<String, Object> params = new HashMap<>();
        public JobParametersBuilder addString(String key, String value) { params.put(key, value); return this; }
        public JobParametersBuilder addLong(String key, Long value) { params.put(key, value); return this; }
        public JobParameters toJobParameters() { return new JobParameters(params); }
    }
}
