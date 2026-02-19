package com.hogwai.batch.core.listener;

public interface SkipListener<T, S> {
    default void onSkipInRead(Throwable t) {}
    default void onSkipInProcess(T item, Throwable t) {}
    default void onSkipInWrite(S item, Throwable t) {}
}
