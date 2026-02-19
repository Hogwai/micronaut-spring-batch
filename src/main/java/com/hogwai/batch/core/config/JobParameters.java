package com.hogwai.batch.core.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable set of parameters used to launch a job. Built via {@link JobParametersBuilder}.
 */
public final class JobParameters {
    private final Map<String, Object> parameters;

    private JobParameters(Map<String, Object> parameters) {
        this.parameters = Map.copyOf(parameters);
    }

    /**
     * Creates a new builder for constructing {@code JobParameters}.
     *
     * @return a new builder instance
     */
    public static JobParametersBuilder builder() {
        return new JobParametersBuilder();
    }

    /**
     * Retrieves a parameter value by key.
     *
     * @param key the parameter key
     * @return the value, or {@code null} if not present
     */
    public Object get(String key) { return parameters.get(key); }

    /**
     * Retrieves a string parameter by key.
     *
     * @param key the parameter key
     * @return the string value, or {@code null} if not present
     */
    public String getString(String key) { return (String) parameters.get(key); }

    /**
     * Retrieves a long parameter by key.
     *
     * @param key the parameter key
     * @return the long value, or {@code null} if not present
     */
    public Long getLong(String key) { return (Long) parameters.get(key); }

    /**
     * Builder for constructing {@link JobParameters} instances.
     */
    public static class JobParametersBuilder {
        private final Map<String, Object> params = new HashMap<>();

        /**
         * Adds a string parameter.
         *
         * @param key   the parameter key
         * @param value the string value
         * @return this builder for chaining
         */
        public JobParametersBuilder addString(String key, String value) { params.put(key, value); return this; }

        /**
         * Adds a long parameter.
         *
         * @param key   the parameter key
         * @param value the long value
         * @return this builder for chaining
         */
        public JobParametersBuilder addLong(String key, Long value) { params.put(key, value); return this; }

        /**
         * Builds an immutable {@link JobParameters} instance from the accumulated parameters.
         *
         * @return the constructed job parameters
         */
        public JobParameters toJobParameters() { return new JobParameters(params); }
    }
}
