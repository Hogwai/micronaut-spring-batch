package com.hogwai.batch.core.definition;

public interface Step {
    String getName();
    void execute() throws Exception;;
}