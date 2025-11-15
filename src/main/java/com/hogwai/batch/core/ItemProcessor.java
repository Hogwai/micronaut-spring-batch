package com.hogwai.batch.core;

public interface ItemProcessor<I, O> {
    O process(I item);
}