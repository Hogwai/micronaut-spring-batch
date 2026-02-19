package com.hogwai.batch.core.runtime;

/**
 * Uniquely identifies a logical job run by its name and generated identifier.
 *
 * @param id      the unique instance identifier
 * @param jobName the name of the job this instance represents
 */
public record JobInstance(long id, String jobName) {}
